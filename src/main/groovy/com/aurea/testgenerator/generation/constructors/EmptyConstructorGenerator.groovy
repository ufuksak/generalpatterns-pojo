package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.generation.TestNodeMethod
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorTypes
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.PackageDeclaration
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

        TypeDeclaration declaredInType = patternMatch.match.findParent(TypeDeclaration).get()
        String className = declaredInType.nameAsString
        PackageDeclaration pd = declaredInType.findCompilationUnit().get().packageDeclaration.get()

        TestNodeMethod typeIsInstantiable = new TestNodeMethod()
        MethodDeclaration typeIsInstantiableTest = JavaParser.parseBodyDeclaration("""
            @Test
            public void test_${className}_IsInstantiable() throws Exception {
                new ${className}(); 
            }                              
        """).asMethodDeclaration()


        typeIsInstantiable.node = typeIsInstantiableTest

        typeIsInstantiable.imports << Imports.JUNIT_TEST

        [typeIsInstantiable]
    }
}
