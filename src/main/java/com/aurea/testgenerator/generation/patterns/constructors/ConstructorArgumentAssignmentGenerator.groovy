package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult
import com.aurea.testgenerator.ast.FieldAssignment
import com.aurea.testgenerator.ast.FieldAssignmentsVisitor
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class ConstructorArgumentAssignmentGenerator extends AbstractConstructorTestGenerator {

    JavaParserFacade solver
    FieldResolver fieldResolver
    ValueFactory valueFactory

    @Autowired
    ConstructorArgumentAssignmentGenerator(JavaParserFacade solver, ValueFactory valueFactory) {
        this.solver = solver
        this.valueFactory = valueFactory
        fieldResolver = new FieldResolver(solver)
    }

    @Override
    protected TestGeneratorResult generate(ConstructorDeclaration cd, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()
        FieldAssignmentsVisitor fieldAssignmentsVisitor = new FieldAssignmentsVisitor(cd)
        Collection<FieldAssignment> fieldAssignments = fieldAssignmentsVisitor.visit()
        if (!fieldAssignments) {
            return result
        }

        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)
        FieldAccessBuilder fieldAccessBuilder = new FieldAccessBuilder(scope)

        AssertionBuilder assertionBuilder = new AssertionBuilder().softly(fieldAssignments.size() > 1)
        fieldAssignments.each {
            addAssertion(it.expr, fieldAccessBuilder, assertionBuilder, result)
        }
        List<DependableNode<Statement>> assertions = assertionBuilder.build()
        if (assertions) {
            DependableNode<MethodDeclaration> assignsArguments = new DependableNode<>()
            List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(cd.parameters).map { p ->
                valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                    new RuntimeException("Failed to build variable for parameter $p of $cd")
                }
            }.toList()
            TestNodeMerger.appendDependencies(assignsArguments, variables)
            TestNodeMerger.appendDependencies(assignsArguments, assertions)
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }

            Map<SimpleName, DependableNode<Expression>> variableExpressionsByNames = StreamEx.of(variables)
                                                                                             .toMap(
                    { it.node.getVariable(0).name },
                    {
                        DependableNode.from(new NameExpr(it.node.getVariable(0).name))
                    })

            Optional<DependableNode<ObjectCreationExpr>> constructorCall = new InvocationBuilder(valueFactory)
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
        
                        ${constructCallExpr.node.type} $instanceName = ${constructCallExpr.node};
                        
                        ${assertions.collect { it.node }.join(System.lineSeparator())}
                    }
                """
                assignsArguments.node = JavaParser.parseBodyDeclaration(assignsArgumentsCode)
                                                  .asMethodDeclaration()
                assignsArguments.dependency.imports << Imports.JUNIT_TEST
                result.tests = [assignsArguments]
            }
        }
        result
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
            result.errors << TestGeneratorError.unsolved(fieldAccessExpr)
        }
    }

    @Override
    protected TestType getType() {
        ConstructorTypes.CONSTRUCTOR_ARGUMENT_ASSIGNMENTS
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        Callability.isCallableFromTests(cd) && ASTNodeUtils.parents(cd, TypeDeclaration).noneMatch { it.enumDeclaration }
    }
}
