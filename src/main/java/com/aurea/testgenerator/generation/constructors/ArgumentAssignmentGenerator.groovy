package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.*
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class ArgumentAssignmentGenerator extends AbstractConstructorTestGenerator {

    FieldAssignments fieldAssignments
    JavaParserFacade solver
    ValueFactory valueFactory

    @Autowired
    ArgumentAssignmentGenerator(FieldAssignments fieldAssignments, JavaParserFacade solver, ValueFactory valueFactory) {
        this.fieldAssignments = fieldAssignments
        this.solver = solver
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(ConstructorDeclaration cd, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()
        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)

        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = fieldAssignments.findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> onlyArgumentAssignExprs = onlyLastAssignExprs.findAll {
            it.value.nameExpr && cd.isNameOfArgument(it.value.asNameExpr().name)
        }
        AssertionBuilder assertionBuilder = new AssertionBuilder().softly(onlyArgumentAssignExprs.size() > 1)
        for (AssignExpr assignExpr : onlyArgumentAssignExprs) {
            FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
            try {
                Optional<ResolvedFieldDeclaration> maybeField = fieldAccessExpr.findField(solver)
                if (maybeField.present) {
                    ResolvedType fieldType = maybeField.get().getType()
                    Optional<Expression> maybeFieldAccessExpression = fieldAssignments.buildFieldAccessExpression(assignExpr, scope)
                    Expression expected = assignExpr.value
                    maybeFieldAccessExpression.ifPresent { fieldAccessExpression ->
                        assertionBuilder.with(fieldType, fieldAccessExpression, expected)
                    }
                } else {
                    result.errors << new TestGeneratorError("Failed to solve field access $fieldAccessExpr")
                }
            } catch (UnsolvedSymbolException use) {
                result.errors << new TestGeneratorError("Failed to solve field access $fieldAccessExpr")
            }
        }
        List<TestNodeStatement> assertions = assertionBuilder.build()
        if (!assertions.empty) {
            TestNodeMethod assignsArguments = new TestNodeMethod()
            List<TestNodeVariable> variables = StreamEx.of(cd.parameters).map { p ->
                valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                    new RuntimeException("Failed to build variable for parameter $p of $cd")
                }
            }.toList()
            TestNodeMerger.appendDependencies(assignsArguments, variables)
            TestNodeMerger.appendDependencies(assignsArguments, assertions)
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }

            Map<SimpleName, TestNodeExpression> variableExpressionsByNames = StreamEx.of(variables)
                                                                                     .toMap(
                    { it.node.getVariable(0).name },
                    {
                        new TestNodeExpression(node: new NameExpr(it.node.getVariable(0).name))
                    })

            Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory)
                    .usingForParameters(variableExpressionsByNames)
                    .build(cd)
            constructorCall.ifPresent { constructCallExpr ->
                TestMethodNomenclature testMethodNomenclature = namerFactory.getTestMethodNomenclature(unitUnderTest.javaClass)
                TestNodeMerger.appendDependencies(assignsArguments, constructCallExpr)
                String testName = testMethodNomenclature.requestTestMethodName(getType(), cd)
                String assignsArgumentsCode = """
                    @Test
                    public void ${testName}() throws Exception {
                        ${variableStatements.join(System.lineSeparator())}
        
                        ${cd.nameAsString} $instanceName = ${constructCallExpr.node};
                        
                        ${assertions.collect { it.node }.join(System.lineSeparator())}
                    }
                """
                assignsArguments.node = JavaParser.parseBodyDeclaration(assignsArgumentsCode)
                                                               .asMethodDeclaration()
                assignsArguments.dependency.imports << Imports.JUNIT_TEST
                result.tests = [ assignsArguments ]
            }
        }
        result
    }

    @Override
    protected TestType getType() {
        ConstructorTypes.ARGUMENT_ASSIGNMENTS
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        Callability.isCallableFromTests(cd) && ASTNodeUtils.parents(cd, TypeDeclaration).noneMatch { it.enumDeclaration }
    }
}
