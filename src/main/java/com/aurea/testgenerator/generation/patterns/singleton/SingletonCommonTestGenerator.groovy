package com.aurea.testgenerator.generation.patterns.singleton

import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class SingletonCommonTestGenerator {

    ValueFactory valueFactory
    NomenclatureFactory nomenclatures

    @Autowired
    SingletonCommonTestGenerator(ValueFactory valueFactory, NomenclatureFactory nomenclatures) {
        this.valueFactory = valueFactory
        this.nomenclatures = nomenclatures
    }

    TestGeneratorResult addSameInstanceTest(MethodDeclaration method, Unit unitUnderTest, TestGeneratorResult result) {
        InvocationBuilder invocationBuilder = new InvocationBuilder(valueFactory)
        Optional<DependableNode<MethodCallExpr>> maybeFirst = invocationBuilder.buildMethodInvocation(method)
        Optional<DependableNode<MethodCallExpr>> maybeOther = invocationBuilder.buildMethodInvocation(method)
        if (maybeFirst.present && maybeOther.present) {
            DependableNode<MethodCallExpr> first = maybeFirst.get()
            DependableNode<MethodCallExpr> other = maybeOther.get()

            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            TestNodeMerger.appendDependencies(testMethod, first)
            TestNodeMerger.appendDependencies(testMethod, other)

            TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
            String testName = testMethodNomenclature.requestTestMethodName(SingletonTypes.SAME_INSTANCE, method)

            String testCode = """
                @Test
                public void ${testName}() throws Exception {
                    ${method.type} first = ${first.node};
                    ${method.type} other = ${other.node};

                    assertThat(first).isSameAs(other);                    
                }
            """

            testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                                        .asMethodDeclaration()
            testMethod.dependency.imports << Imports.JUNIT_TEST
            testMethod.dependency.imports << Imports.ASSERTJ_ASSERTTHAT
            result.tests << testMethod
        }
        result
    }

    TestGeneratorResult addThreadSafetyTest(MethodDeclaration method, Unit unitUnderTest, TestGeneratorResult result) {
        InvocationBuilder invocationBuilder = new InvocationBuilder(valueFactory)
        invocationBuilder.buildMethodInvocation(method).ifPresent { invocation ->
            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            TestNodeMerger.appendDependencies(testMethod, invocation)
            String typeAsString = method.type.toString()

            TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
            String testName = testMethodNomenclature.requestTestMethodName(SingletonTypes.THREAD_SAFE, method)

            String testCode = """
                @Test
                public void ${testName}() throws Exception {
                    SingletonTester tester = SingletonTester.fromSingleton(new Callable<${typeAsString}>() {
                        @Override
                        public ${typeAsString} call() throws Exception {
                            return ${invocation.node};
                        }
                    });
                    
                    tester.testThreadSafety();
                }
            """

            testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                                        .asMethodDeclaration()
            testMethod.dependency.imports << Imports.JUNIT_TEST
            testMethod.dependency.imports << Imports.CALLABLE
            testMethod.dependency.imports << Imports.SINGLETON_TESTER
            result.tests << testMethod
        }
        result
    }
}
