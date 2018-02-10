package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType

class ArbitraryPrimitiveValuesFactory implements PrimitiveValueFactory {

    static Map<PrimitiveType, TestNodeExpression> ARBITRARY_EXPRESSIONS = [
            (PrimitiveType.booleanType()): new TestNodeExpression(node: new BooleanLiteralExpr(true)),
            (PrimitiveType.charType())   : new TestNodeExpression(node: new CharLiteralExpr('c')),
            (PrimitiveType.byteType())   : new TestNodeExpression(node: new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.shortType())  : new TestNodeExpression(node: new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.intType())    : new TestNodeExpression(node: new IntegerLiteralExpr(42)),
            (PrimitiveType.longType())   : new TestNodeExpression(node: new LongLiteralExpr("42L")),
            (PrimitiveType.floatType())  : new TestNodeExpression(node: new DoubleLiteralExpr(42.0)),
            (PrimitiveType.doubleType()) : new TestNodeExpression(node: new DoubleLiteralExpr(42.0)),
    ]

    @Override
    TestNodeExpression get(PrimitiveType type) {
        ARBITRARY_EXPRESSIONS[type]
    }
}
