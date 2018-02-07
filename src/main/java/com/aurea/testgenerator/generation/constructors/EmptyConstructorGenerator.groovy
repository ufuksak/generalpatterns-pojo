package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorPatterns
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.ToString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ToString
class EmptyConstructorGenerator implements PatternToTest {

    ValueFactory valueFactory

    @Autowired
    EmptyConstructorGenerator(ValueFactory valueFactory) {
        this.valueFactory = valueFactory
    }

    @Override
    void accept(PatternMatch patternMatch, TestUnit testUnit) {
        if (patternMatch.pattern != ConstructorPatterns.IS_EMPTY) {
            return
        }

        ConstructorDeclaration cd = patternMatch.match.asConstructorDeclaration()
        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory).build(cd)
        constructorCall.ifPresent { constructCallExpr ->
            MethodDeclaration typeIsInstantiableTest = JavaParser.parseBodyDeclaration("""
            @Test
            public void test_${cd.nameAsString}_IsInstantiable() throws Exception {
                ${constructCallExpr.expr};
            }                              
            """).asMethodDeclaration()
            testUnit.addImport Imports.JUNIT_TEST
            testUnit.addTest typeIsInstantiableTest
        }
    }
}
