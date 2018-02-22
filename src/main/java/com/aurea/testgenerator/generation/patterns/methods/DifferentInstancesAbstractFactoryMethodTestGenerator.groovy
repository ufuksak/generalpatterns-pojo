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
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class DifferentInstancesAbstractFactoryMethodTestGenerator extends AbstractMethodTestGenerator {

    ValueFactory valueFactory

    @Autowired
    DifferentInstancesAbstractFactoryMethodTestGenerator(JavaParserFacade solver,
                                                         TestGeneratorResultReporter reporter,
                                                         VisitReporter visitReporter,
                                                         NomenclatureFactory nomenclatures,
                                                         ValueFactory valueFactory) {
        super(solver, reporter, visitReporter, nomenclatures)
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest) {
        InvocationBuilder invocationBuilder = new InvocationBuilder(valueFactory)
        Optional<DependableNode<MethodCallExpr>> maybeFirst = invocationBuilder.buildMethodInvocation(method)
        Optional<DependableNode<MethodCallExpr>> maybeOther = invocationBuilder.buildMethodInvocation(method)
        TestGeneratorResult result = new TestGeneratorResult()
        if (maybeFirst && maybeOther) {
            DependableNode<MethodCallExpr> first = maybeFirst.get()
            DependableNode<MethodCallExpr> other = maybeOther.get()

            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            TestNodeMerger.appendDependencies(testMethod, first)
            TestNodeMerger.appendDependencies(testMethod, other)

            TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
            String testName = testMethodNomenclature.requestTestMethodName(getType(), method)

            String testCode = """
                @Test
                public void ${testName}() throws Exception {
                    ${method.type} first = ${first.node};
                    ${method.type} other = ${other.node};

                    assertThat(first).isNotSameAs(other);                    
                }
            """

            testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                                        .asMethodDeclaration()
            testMethod.dependency.imports << Imports.JUNIT_TEST
            testMethod.dependency.imports << Imports.ASSERTJ_ASSERTTHAT
            result.tests = [testMethod]
        }
        result
    }

    @Override
    protected TestType getType() {
        AbstractFactoryMethodTypes.DIFFERENT_INSTANCES
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
