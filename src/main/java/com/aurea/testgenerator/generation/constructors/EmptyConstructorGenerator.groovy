package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestNodeMethod
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorTypes
import com.aurea.testgenerator.value.random.RandomValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.ToString
import org.springframework.stereotype.Component

@Component
@ToString
class EmptyConstructorGenerator implements UnitTestGenerator {

    @Override
    List<TestNodeMethod> apply(PatternMatch patternMatch) {
        if (patternMatch.type != ConstructorTypes.EMPTY) {
            return Collections.emptyList()
        }

        ConstructorDeclaration cd = patternMatch.match.asConstructorDeclaration()
        TestNodeMethod typeIsInstantiable = new TestNodeMethod()
        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(new RandomValueFactory()).build(cd)
        List<TestNodeMethod> testMethods = []
        constructorCall.ifPresent { constructCallExpr ->
            MethodDeclaration typeIsInstantiableTest = JavaParser.parseBodyDeclaration("""
            @Test
            public void test_${cd.nameAsString}_IsInstantiable() throws Exception {
                ${constructCallExpr.expr};
            }                              
            """).asMethodDeclaration()

            typeIsInstantiable.md = typeIsInstantiableTest
            typeIsInstantiable.dependency.imports << Imports.JUNIT_TEST
            testMethods << typeIsInstantiable
        }
        testMethods
    }
}
