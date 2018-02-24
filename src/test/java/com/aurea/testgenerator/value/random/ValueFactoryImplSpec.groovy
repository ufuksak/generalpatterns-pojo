package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.TestUnitSpec
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.value.ArbitraryPrimitiveValuesFactory
import com.aurea.testgenerator.value.ArbitraryReferenceTypeFactory
import com.aurea.testgenerator.value.PrimitiveValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import spock.lang.Unroll

class ValueFactoryImplSpec extends TestUnitSpec {

    ValueFactoryImpl factory

    def setup() {
        PrimitiveValueFactory arbitraryPrimitiveValues = new ArbitraryPrimitiveValuesFactory()
        ArbitraryReferenceTypeFactory javaLangTypesFactory = new ArbitraryReferenceTypeFactory()
        javaLangTypesFactory.arbitraryPrimitiveValuesFactory = arbitraryPrimitiveValues
        factory = new ValueFactoryImpl(javaLangTypesFactory, arbitraryPrimitiveValues)
    }

    @Unroll
    def "should be able to build variables of primitive types"() {
        expect:
        Type wrapped = wrapWithCompilationUnit(type)
        Optional<DependableNode<VariableDeclarationExpr>> testNodeVariableOptional = factory.getVariable("foo", wrapped)
        testNodeVariableOptional.present
        String expression = testNodeVariableOptional.get().node.toString()
        expression == expected

        where:
        type                        | expected
        PrimitiveType.booleanType() | "boolean foo = true"
        PrimitiveType.byteType()    | "byte foo = (byte) 42"
        PrimitiveType.charType()    | "char foo = 'c'"
        PrimitiveType.shortType()   | "short foo = (short) 42"
        PrimitiveType.intType()     | "int foo = 42"
        PrimitiveType.longType()    | "long foo = 42L"
        PrimitiveType.floatType()   | "float foo = 42.0"
        PrimitiveType.doubleType()  | "double foo = 42.0"
    }

    def "should be able to build variable for strings"() {
        expect:
        Type type = wrapWithCompilationUnit(JavaParser.parseClassOrInterfaceType("String"))
        Optional<DependableNode<VariableDeclarationExpr>> testNodeVariableOptional = factory.getVariable("foo", type)
        testNodeVariableOptional.present
        String expression = testNodeVariableOptional.get().node.toString()
        expression == 'String foo = "ABC"'
    }
}
