package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult
import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestNodeMethod
import com.aurea.testgenerator.generation.TestNodeStatement
import com.aurea.testgenerator.generation.TestNodeVariable
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class ArgumentAssignmentGenerator extends AbstractConstructorTestGenerator {

    JavaParserFacade solver
    FieldResolver fieldResolver
    ValueFactory valueFactory

    @Autowired
    ArgumentAssignmentGenerator(JavaParserFacade solver, ValueFactory valueFactory) {
        this.solver = solver
        this.valueFactory = valueFactory
        fieldResolver = new FieldResolver(solver)
    }

    @Override
    protected TestGeneratorResult generate(ConstructorDeclaration cd, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()
        Collection<AssignExpr> argumentAssignExpressions = findArgumentAssignExpressions(cd)
        if (!argumentAssignExpressions) {
            return result
        }

        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)
        FieldAccessBuilder fieldAccessBuilder = new FieldAccessBuilder(scope)

        AssertionBuilder assertionBuilder = new AssertionBuilder().softly(argumentAssignExpressions.size() > 1)
        argumentAssignExpressions.each {
            addAssertion(it, fieldAccessBuilder, assertionBuilder, result)
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
                TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
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

    private static Collection<AssignExpr> findArgumentAssignExpressions(ConstructorDeclaration cd) {
        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = FieldAssignments.findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> onlyArgumentAssignExprs = onlyLastAssignExprs.findAll {
            it.value.nameExpr && cd.isNameOfArgument(it.value.asNameExpr().name)
        }
        onlyArgumentAssignExprs
    }

    private void addAssertion(AssignExpr assignExpr,
                              FieldAccessBuilder fieldAccessBuilder,
                              AssertionBuilder assertionBuilder,
                              TestGeneratorResult result) {
        FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
        Optional<ResolvedFieldDeclaration> maybeField = fieldResolver.resolve(fieldAccessExpr)
        if (maybeField.present) {
            ResolvedFieldDeclaration field = maybeField.get()
            FieldAccessResult fieldAccessResult = fieldAccessBuilder.build(field)
            if (fieldAccessResult.type == FieldAccessResult.Type.SUCCESS) {
                ResolvedType fieldType = field.getType()
                Expression expected = assignExpr.value
                assertionBuilder.with(fieldType, fieldAccessResult.expression, expected)
            } else if (fieldAccessResult.type == FieldAccessResult.Type.FAILED) {
                result.errors << fieldAccessResult.error
            }
        } else {
            result.errors << new TestGeneratorError("Failed to solve field access $fieldAccessExpr")
        }
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
