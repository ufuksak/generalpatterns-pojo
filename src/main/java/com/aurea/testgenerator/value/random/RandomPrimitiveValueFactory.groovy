package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.generation.TestNodeExpression
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.CastExpr
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.type.PrimitiveType
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils

import static com.github.javaparser.ast.type.PrimitiveType.Primitive.*


class RandomPrimitiveValueFactory {
    static TestNodeExpression get(PrimitiveType type) {
        switch (type.getType()) {
            case BOOLEAN:
                return new TestNodeExpression(node: new BooleanLiteralExpr(RandomUtils.nextBoolean()))
            case CHAR:
                return new TestNodeExpression(node: new CharLiteralExpr(RandomStringUtils.randomAlphabetic(1)))
            case BYTE:
                CastExpr castExpr = new CastExpr(PrimitiveType.booleanType(), new IntegerLiteralExpr(RandomUtils.nextInt(-127, 127)))
                return new TestNodeExpression(node: castExpr)
            case SHORT:
                CastExpr castExpr = new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(RandomUtils.nextInt(-127, 127)))
                return new TestNodeExpression(node: castExpr)
            case INT:
                return new TestNodeExpression(node: new IntegerLiteralExpr(RandomUtils.nextInt()))
            case LONG:
                return new TestNodeExpression(node: new LongLiteralExpr(RandomUtils.nextLong()))
            case FLOAT:
                return new TestNodeExpression(node: new DoubleLiteralExpr(RandomUtils.nextFloat()))
            case DOUBLE:
                return new TestNodeExpression(node: new DoubleLiteralExpr(RandomUtils.nextDouble()))
        }
    }
}
