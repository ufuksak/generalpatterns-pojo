package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType

class ArbitraryPrimitiveValuesFactory implements PrimitiveValueFactory {

    static Map<PrimitiveType, TestNodeExpression> ARBITRARY_EXPRESSIONS = [
            (PrimitiveType.booleanType()): new TestNodeExpression(expr: new BooleanLiteralExpr(true)),
            (PrimitiveType.charType())   : new TestNodeExpression(expr: new CharLiteralExpr('c')),
            (PrimitiveType.byteType())   : new TestNodeExpression(expr: new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.shortType())  : new TestNodeExpression(expr: new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.intType())    : new TestNodeExpression(expr: new IntegerLiteralExpr(42)),
            (PrimitiveType.longType())   : new TestNodeExpression(expr: new LongLiteralExpr("42L")),
            (PrimitiveType.floatType())  : new TestNodeExpression(expr: new DoubleLiteralExpr(42.0)),
            (PrimitiveType.doubleType()) : new TestNodeExpression(expr: new DoubleLiteralExpr(42.0)),
    ]

    @Override
    TestNodeExpression get(PrimitiveType type) {
        ARBITRARY_EXPRESSIONS[type]
    }
}
