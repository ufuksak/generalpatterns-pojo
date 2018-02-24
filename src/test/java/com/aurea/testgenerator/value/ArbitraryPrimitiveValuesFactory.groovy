package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.CastExpr
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.resolution.types.ResolvedPrimitiveType

class ArbitraryPrimitiveValuesFactory implements PrimitiveValueFactory {

    static Map<PrimitiveType, DependableNode<Expression>> ARBITRARY_EXPRESSIONS = [
            (PrimitiveType.booleanType()): new DependableNode(node: new BooleanLiteralExpr(true)),
            (PrimitiveType.charType())   : new DependableNode(node: new CharLiteralExpr('c')),
            (PrimitiveType.byteType())   : new DependableNode(node: new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.shortType())  : new DependableNode(node: new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(42))),
            (PrimitiveType.intType())    : new DependableNode(node: new IntegerLiteralExpr(42)),
            (PrimitiveType.longType())   : new DependableNode(node: new LongLiteralExpr("42L")),
            (PrimitiveType.floatType())  : new DependableNode(node: new DoubleLiteralExpr(42.0)),
            (PrimitiveType.doubleType()) : new DependableNode(node: new DoubleLiteralExpr(42.0)),
    ]

    static Map<ResolvedPrimitiveType, DependableNode<Expression>> RESOLVED_ARBITRARY_EXPRESSIONS = [
            (ResolvedPrimitiveType.BOOLEAN): ARBITRARY_EXPRESSIONS[PrimitiveType.booleanType()],
            (ResolvedPrimitiveType.CHAR)   : ARBITRARY_EXPRESSIONS[PrimitiveType.charType()],
            (ResolvedPrimitiveType.BYTE)   : ARBITRARY_EXPRESSIONS[PrimitiveType.byteType()],
            (ResolvedPrimitiveType.SHORT)  : ARBITRARY_EXPRESSIONS[PrimitiveType.shortType()],
            (ResolvedPrimitiveType.INT)    : ARBITRARY_EXPRESSIONS[PrimitiveType.intType()],
            (ResolvedPrimitiveType.LONG)   : ARBITRARY_EXPRESSIONS[PrimitiveType.longType()],
            (ResolvedPrimitiveType.FLOAT)  : ARBITRARY_EXPRESSIONS[PrimitiveType.floatType()],
            (ResolvedPrimitiveType.DOUBLE) : ARBITRARY_EXPRESSIONS[PrimitiveType.doubleType()]
    ]

    @Override
    DependableNode<Expression> get(PrimitiveType type) {
        ARBITRARY_EXPRESSIONS[type]
    }

    @Override
    DependableNode<Expression> get(ResolvedPrimitiveType type) {
        RESOLVED_ARBITRARY_EXPRESSIONS[type]
    }
}
