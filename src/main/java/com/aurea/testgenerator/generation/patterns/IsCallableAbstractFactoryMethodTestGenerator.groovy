package com.aurea.testgenerator.generation.patterns

import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.Type
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class IsCallableAbstractFactoryMethodTestGenerator extends AbstractMethodTestGenerator {

    ValueFactory valueFactory

    @Autowired
    IsCallableAbstractFactoryMethodTestGenerator(ValueFactory valueFactory) {
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest) {
        Optional<DependableNode<Expression>> methodCall = new InvocationBuilder(valueFactory).buildMethodInvocation(method)
        TestGeneratorResult result = new TestGeneratorResult()
        methodCall.ifPresent { methodCallExpression ->
            TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
            DependableNode<MethodDeclaration> methodCallTest = new DependableNode<>()
            TestNodeMerger.appendDependencies(methodCallTest, methodCallExpression)
            String testText = ''
            String testName = testMethodNomenclature.requestTestMethodName(AbstractFactoryMethodTypes.IS_CALLABLE, method)
            try {
                testText = """
                        @Test
                        public void ${testName}() throws Exception {
                            ${methodCallExpression.node};
                        }                              
                        """
                methodCallTest.node = JavaParser.parseBodyDeclaration(testText).asMethodDeclaration()
                methodCallTest.dependency.imports << Imports.JUNIT_TEST

                result.tests = [methodCallTest]
            } catch (ParseProblemException ppe) {
                log.error "Failed to parse $testText", ppe
                result.errors << new TestGeneratorError(cause: "Failed to parse $testText")
            }
        }
        result
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, MethodDeclaration method) {
        return super.shouldBeVisited(unit, method) &&
                Callability.isCallableFromTests(method) &&
                method.static &&
                returnsCreatedClassOrInterface(method)
    }

    private static boolean returnsCreatedClassOrInterface(MethodDeclaration method) {
        Type returnType = method.type
        if (!returnType?.classOrInterfaceType) {
            return false
        }
        if (!method.findAll(ObjectCreationExpr) { it.type == returnType }) {
            return false
        }
        return true
    }

    @Override
    protected TestType getType() {
        return AbstractFactoryMethodTypes.IS_CALLABLE
    }
}
