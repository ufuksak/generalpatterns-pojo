package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.value.PrimitiveValueFactory
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.springframework.stereotype.Component

import static com.github.javaparser.ast.type.PrimitiveType.Primitive.*

@Component
class RandomPrimitiveValueFactory implements PrimitiveValueFactory {
    TestNodeExpression get(PrimitiveType type) {
        switch (type.getType()) {
            case BOOLEAN:
                return new TestNodeExpression(node: new BooleanLiteralExpr(RandomUtils.nextBoolean()))
            case CHAR:
                return new TestNodeExpression(node: new CharLiteralExpr(RandomStringUtils.randomAlphabetic(1)))
            case BYTE:
                CastExpr castExpr = new CastExpr(PrimitiveType.byteType(), new IntegerLiteralExpr(RandomUtils.nextInt(-127, 127)))
                return new TestNodeExpression(node: castExpr)
            case SHORT:
                CastExpr castExpr = new CastExpr(PrimitiveType.shortType(), new IntegerLiteralExpr(RandomUtils.nextInt(-127, 127)))
                return new TestNodeExpression(node: castExpr)
            case INT:
                return new TestNodeExpression(node: new IntegerLiteralExpr(RandomUtils.nextInt()))
            case LONG:
                return new TestNodeExpression(node: new LongLiteralExpr(RandomUtils.nextLong().toString() + "L"))
            case FLOAT:
                return new TestNodeExpression(node: new DoubleLiteralExpr(RandomUtils.nextFloat()))
            case DOUBLE:
                return new TestNodeExpression(node: new DoubleLiteralExpr(RandomUtils.nextDouble()))
        }
        return null
    }
}
