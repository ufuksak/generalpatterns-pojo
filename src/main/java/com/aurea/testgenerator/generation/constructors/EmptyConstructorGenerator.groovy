package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.*
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.ToString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ToString
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
            MethodDeclaration typeIsInstantiableTest = JavaParser.parseBodyDeclaration("""
            @Test
            public void test_${cd.nameAsString}_IsInstantiable() throws Exception {
                ${constructCallExpr.expr};
            }                              
            """).asMethodDeclaration()

            result.tests = [new TestNodeMethod(
                    dependency: new TestDependency(imports: [Imports.JUNIT_TEST]),
                    md: typeIsInstantiableTest
            )]
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
