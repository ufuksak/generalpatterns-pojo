package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

import java.nio.file.Paths


class MethodVisitorSpec extends Specification {

    def "Nesting increments"() {
        expect:
        MethodMetaInformation meta = fromMethod '''
            void foo() {
                if (condition1) { 
                    for (int i = 0; i < 10; i++) { 
                        while (condition2) {} 
                    }
                }
            }
        '''

        !meta.isAbstract
        meta.locs == 4
        meta.cognitiveComplexity == 7
    }

    MethodMetaInformation fromMethod(String methodCode) {
        def method = UnitHelper.getMethodFromSource(methodCode)
        def visitor = new MethodVisitor()
        visitor.visit(method, new Unit(modulePath: Paths.get("dummy")))
        visitor.methodMetaInformations[0]
    }
}
