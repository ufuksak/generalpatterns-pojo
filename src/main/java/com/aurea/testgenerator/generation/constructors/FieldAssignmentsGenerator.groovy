package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorTypes
import com.aurea.testgenerator.value.random.RandomValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component


@Component
@Log4j2
class FieldAssignmentsGenerator implements PatternToTest {

    @Override
    void accept(PatternMatch patternMatch, TestUnit testUnit) {
        if (patternMatch.type != ConstructorTypes.FIELD_LITERAL_ASSIGNMENTS) {
            return
        }

        ConstructorDeclaration cd = patternMatch.match.asConstructorDeclaration()
        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(new RandomValueFactory()).build(cd)
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
