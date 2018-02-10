package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.*
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.ConstructorDeclaration
import groovy.transform.ToString
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ToString
@Log4j2
class EmptyConstructorGenerator extends AbstractConstructorTestGenerator {

    ValueFactory valueFactory

    @Autowired
    EmptyConstructorGenerator(ValueFactory valueFactory) {
        this.valueFactory = valueFactory
    }

    @Override
    TestGeneratorResult generate(ConstructorDeclaration cd) {
        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory).build(cd)
        TestGeneratorResult result = new TestGeneratorResult()
        constructorCall.ifPresent { constructCallExpr ->
            TestNodeMethod isInstantiable = new TestNodeMethod()
            TestNodeMerger.appendDependencies(isInstantiable, constructCallExpr)
            String testText = ''
            try {
                testText = """
                @Test
                public void test_${cd.nameAsString}_IsInstantiable() throws Exception {
                    ${constructCallExpr.node};
                }                              
                """
                isInstantiable.node = JavaParser.parseBodyDeclaration(testText)
                                                  .asMethodDeclaration()
                isInstantiable.dependency.imports << Imports.JUNIT_TEST

                result.tests = [isInstantiable]
            } catch (ParseProblemException ppe) {
                log.error "Failed to parse $testText", ppe
                result.errors << new TestGeneratorError(cause: "Failed to parse $testText")
            }
        }
        result
    }

    @Override
    TestType getType() {
        ConstructorTypes.EMPTY_CONSTRUCTOR
    }

    @Override
    boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        Callability.isCallableFromTests(cd)
    }
}
