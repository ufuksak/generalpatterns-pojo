package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.UnitTest
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.type.Type

import static com.github.javaparser.JavaParser.parseExpression

class Assertions {
    static final FLOATING_POINT_OFFSET_FLOAT = '0.001F'
    static final FLOATING_POINT_OFFSET_DOBLE = '0.001D'
    static final FLOATING_POINT_TYPES_FLOAT = ['float', 'Float', 'java.lang.Float']
    static final FLOATING_POINT_TYPES_DOUBLE = ['double', 'Double', 'java.lang.Double']

    static MethodCallExpr appendPrimitiveAssertion(UnitTest ut, Type type, Expression actual, Expression expected) {
        MethodCallExpr result
        switch (type.toString()) {
            case FLOATING_POINT_TYPES_FLOAT:
                ut.imports << Imports.ASSERTJ_OFFSET
                result = parseExpression("assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_FLOAT));").asMethodCallExpr()
                break
            case FLOATING_POINT_TYPES_DOUBLE:
                ut.imports << Imports.ASSERTJ_OFFSET
                result = parseExpression("assertThat($actual).isCloseTo($expected, Offset.offset($FLOATING_POINT_OFFSET_DOBLE));").asMethodCallExpr()
                break
            default:
                result = parseExpression("assertThat($actual).isEqualTo($expected);")

        }
        ut.imports << Imports.ASSERTJ_ASSERTTHAT
        return result
    }
}
