package com.aurea.ast.common

import com.aurea.testgenerator.source.JavaClass
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
        def javaFileCode = """
package ${TEST_CLASS.package};

public class ${TEST_CLASS.name} {

$code

}
"""
        getUnitForCode(javaFileCode)
    }

    static CompilationUnit getUnitForCode(String code) {
        try {
            JavaParser.parse(code)
        } catch (Exception e) {
            log.error("Failed to parse code: $code", e)
            throw new IllegalArgumentException(e)
        }
    }

    static CompilationUnit getUnitForCode(File file) {
        try {
            JavaParser.parse(file)
        } catch (Exception e) {
            log.error("Failed to parse file: $file", e)
            throw new IllegalArgumentException(e)
        }
    }
}
