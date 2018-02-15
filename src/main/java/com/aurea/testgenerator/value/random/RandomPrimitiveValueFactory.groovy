package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.value.PrimitiveValueFactory
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.CastExpr
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.springframework.stereotype.Component

import static com.github.javaparser.ast.type.PrimitiveType.Primitive.BOOLEAN
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.BYTE
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.CHAR
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.DOUBLE
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.FLOAT
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.INT
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.LONG
import static com.github.javaparser.ast.type.PrimitiveType.Primitive.SHORT

@Component
class RandomPrimitiveValueFactory implements PrimitiveValueFactory {
    @Override
    DependableNode<Expression> get(PrimitiveType type) {
        switch (type.getType()) {
            case BOOLEAN:
                return DependableNode.from(new BooleanLiteralExpr(RandomUtils.nextBoolean()))
            case CHAR:
                return DependableNode.from(new CharLiteralExpr(RandomStringUtils.randomAlphabetic(1)))
            case BYTE:
                CastExpr castExpr = new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE)))
                return DependableNode.from(castExpr)
            case SHORT:
                CastExpr castExpr = new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(nextInt(Short.MIN_VALUE, Short.MAX_VALUE)))
                return DependableNode.from(castExpr)
            case INT:
                return DependableNode.from(new IntegerLiteralExpr(RandomUtils.nextInt()))
            case LONG:
                return DependableNode.from(new LongLiteralExpr(RandomUtils.nextLong().toString() + "L"))
            case FLOAT:
                return DependableNode.from(new DoubleLiteralExpr(RandomUtils.nextFloat()))
            case DOUBLE:
                return DependableNode.from(new DoubleLiteralExpr(RandomUtils.nextDouble()))
        }
        throw new UnsupportedOperationException("Unknown primitive type: $type")
    }

    @Override
    DependableNode<Expression> get(ResolvedPrimitiveType type) {
        if (type == ResolvedPrimitiveType.BOOLEAN) {
            return DependableNode.from(new BooleanLiteralExpr(RandomUtils.nextBoolean()))
        } else if (type == ResolvedPrimitiveType.CHAR) {
            return DependableNode.from(new CharLiteralExpr(RandomStringUtils.randomAlphabetic(1)))
        } else if (type == ResolvedPrimitiveType.BYTE) {
            CastExpr castExpr = new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE)))
            return DependableNode.from(castExpr)
        } else if (type == ResolvedPrimitiveType.SHORT) {
            CastExpr castExpr = new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(nextInt(Short.MIN_VALUE, Short.MAX_VALUE)))
            return DependableNode.from(castExpr)
        } else if (type == ResolvedPrimitiveType.INT) {
            return DependableNode.from(new IntegerLiteralExpr(RandomUtils.nextInt()))
        } else if (type == ResolvedPrimitiveType.LONG) {
            return DependableNode.from(new LongLiteralExpr(RandomUtils.nextLong().toString() + "L"))
        } else if (type == ResolvedPrimitiveType.FLOAT) {
            return DependableNode.from(new DoubleLiteralExpr(RandomUtils.nextFloat().toString() + "F"))
        } else if (type == ResolvedPrimitiveType.DOUBLE) {
            return DependableNode.from(new DoubleLiteralExpr(RandomUtils.nextDouble()))
        }
        throw new UnsupportedOperationException("Unknown primitive type: $type")
    }

    private static int nextInt(int startInclusive, int endExclusive) {
        RandomUtils.nextInt(0, endExclusive - startInclusive) + startInclusive
    }
}
