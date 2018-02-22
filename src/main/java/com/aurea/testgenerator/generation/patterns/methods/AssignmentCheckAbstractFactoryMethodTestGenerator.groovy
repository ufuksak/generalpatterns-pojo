package com.aurea.testgenerator.generation.patterns.methods

import com.aurea.common.CognitiveComplexityCalculator
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.patterns.constructors.Pojos
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class AssignmentCheckAbstractFactoryMethodTestGenerator extends AbstractMethodTestGenerator {

    ValueFactory valueFactory

    @Autowired
    AssignmentCheckAbstractFactoryMethodTestGenerator(JavaParserFacade solver,
                                                      TestGeneratorResultReporter reporter,
                                                      VisitReporter visitReporter,
                                                      NomenclatureFactory nomenclatures,
                                                      ValueFactory valueFactory) {
        super(solver, reporter, visitReporter, nomenclatures)
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()

        Expression returnExpr = method.findAll(ReturnStmt).first().expression.get()
        StateChangeAssertionBuilder assertionBuilder = new StateChangeAssertionBuilder(returnExpr, method, solver, result)
        List<DependableNode<Statement>> assertions = assertionBuilder.buildAssertions()
        if (assertions) {
            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            TestNodeMerger.appendDependencies(testMethod, assertions)

            List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(method.parameters).map { p ->
                valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                    new RuntimeException("Failed to build variable for parameter $p of $method")
                }
            }.toList()
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }
            TestNodeMerger.appendDependencies(testMethod, variables)

            Map<SimpleName, DependableNode<Expression>> variableExpressionsByNames = StreamEx.of(variables)
                                                                                             .toMap(
                    { it.node.getVariable(0).name },
                    { DependableNode.from(new NameExpr(it.node.getVariable(0).name)) })

            Optional<DependableNode<MethodCallExpr>> methodCall = new InvocationBuilder(valueFactory)
                    .usingForParameters(variableExpressionsByNames)
                    .buildMethodInvocation(method)

            methodCall.ifPresent { methodCallExpr ->
                TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
                TestNodeMerger.appendDependencies(testMethod, methodCallExpr)
                String testName = testMethodNomenclature.requestTestMethodName(getType(), method)
                String testCode = """
                    @Test
                    public void ${testName}() throws Exception {
                        ${variableStatements.join(System.lineSeparator())}
        
                        ${method.type} $returnExpr = ${methodCallExpr.node};
                        
                        ${assertions.collect { it.node }.join(System.lineSeparator())}
                    }
                """
                testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                                            .asMethodDeclaration()
                testMethod.dependency.imports << Imports.JUNIT_TEST
                result.tests = [testMethod]
            }

        }
        result
    }

    @Override
    protected TestType getType() {
        return AbstractFactoryMethodTypes.ASSIGNMENT_CHECK
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, MethodDeclaration method) {
        super.shouldBeVisited(unit, method) &&
                Callability.isCallableFromTests(method) &&
                method.static &&
                returnsCreatedClassOrInterface(method) &&
                isC0(method) &&
                hasOnlySetterCalls(method) &&
                hasOnlyOneReturn(method)
    }

    private static boolean returnsCreatedClassOrInterface(MethodDeclaration method) {
        Type returnType = method.type
        returnType?.classOrInterfaceType && method.findAll(ObjectCreationExpr) { it.type == returnType }
    }

    private static boolean isC0(MethodDeclaration method) {
        return !method.abstract &&
                method.body.present &&
                CognitiveComplexityCalculator.calculate(method) == 0
    }

    private static boolean hasOnlySetterCalls(MethodDeclaration method) {
        boolean hasNonSetterCalls = method.findAll(MethodCallExpr).any { it -> !Pojos.isSetterCall(it)}
        !hasNonSetterCalls
    }

    private static hasOnlyOneReturn(MethodDeclaration method) {
        method.findAll(ReturnStmt).findAll { it.expression.present }.size() == 1
    }
}
