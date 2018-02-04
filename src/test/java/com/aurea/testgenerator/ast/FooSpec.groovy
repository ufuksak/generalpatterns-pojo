package com.aurea.testgenerator.ast

import com.aurea.testgenerator.generation.source.Annotations
import com.github.generator.xml.Converters
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import spock.lang.Specification

import java.lang.annotation.Annotation


class FooSpec extends Specification {

    def "sss"() {
        expect:
        CompilationUnit cu = JavaParser.parse("""
            class Foo {
                void foo() {
                    this.foo = 123;
                    }
            }
        """)
        println Converters.newConverter().toXmlString(cu)
    }
}
