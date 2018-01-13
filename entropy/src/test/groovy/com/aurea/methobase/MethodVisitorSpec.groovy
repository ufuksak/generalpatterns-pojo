package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.aurea.methobase.meta.JavaParserFacadeFactory
import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
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
        Optional<CompilationUnit> maybeUnit = UnitHelper.getUnitForMethod(methodCode)
        CompilationUnit unit = maybeUnit.orElseThrow { throw new IllegalArgumentException("Faled to parse code: $methodCode") }
        MethodDeclaration md = unit.findAll(MethodDeclaration).first()
        JavaParserFacadeFactory factory = new JavaParserFacadeFactory([])
        MethodVisitor visitor = new MethodVisitor(factory)
        visitor.visit(md, new Unit(modulePath: Paths.get("dummy")))
        visitor.methodMetaInformations[0]
    }
}
