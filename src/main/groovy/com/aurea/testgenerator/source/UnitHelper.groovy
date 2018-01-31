package com.aurea.testgenerator.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.util.logging.Log4j2

@Log4j2
final class UnitHelper {

    static final TEST_CLASS_NAME = "Sample"
    static final PACKAGE_NAME = "com.aurea.sample"
    static final TEST_CLASS = new JavaClass('com.aurea.sample.Sample')

    static MethodDeclaration getMethodFromSource(String code) {
        CompilationUnit cu = getUnitForMethod(code)
        cu.findFirst(MethodDeclaration).get()
    }

    static CompilationUnit getUnitForMethod(String code) {
        String javaFileCode = """
package ${TEST_CLASS.package};

public class ${TEST_CLASS.name} {

$code

}
"""
        JavaParser.parse(javaFileCode)
    }
}
