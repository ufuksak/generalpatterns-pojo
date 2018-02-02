package com.aurea.testgenerator.ast

import com.github.generator.xml.Converters
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import spock.lang.Specification


class FooSpec extends Specification {

    def "sss"() {
        expect:
        CompilationUnit cu = JavaParser.parse("""
            class Foo {
                void foo() {                    
             Map mapa = new HashMap();
             mapa.put(1, 1);
            }
                
            }
        """)

        println Converters.newConverter().toXmlString(cu)
    }
}
