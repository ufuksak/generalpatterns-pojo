package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.value.ClassOrInterfaceTypeFactory
import com.aurea.testgenerator.value.PrimitiveValueFactory
import com.aurea.testgenerator.value.Types
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import org.apache.commons.lang.math.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RandomJavaLangTypesFactory implements ClassOrInterfaceTypeFactory {

    ValueFactory valueFactory
    PrimitiveValueFactory primitiveValueFactory

    @Autowired
    RandomJavaLangTypesFactory(PrimitiveValueFactory primitiveValueFactory) {
        this.primitiveValueFactory = primitiveValueFactory
    }

    Optional<TestNodeExpression> get(ClassOrInterfaceType type) {
        if (type.boxedType) {
            return Optional.of(primitiveValueFactory.get(type.toUnboxedType()))
        } else if (Types.isString(type)) {
            return Optional.of(new TestNodeExpression(expr: new StringLiteralExpr(RandomStringPool.next())))
        } else if (Types.isList(type) || Types.isCollection(type) || Types.isIterable(type)) {
            Optional<TestNodeExpression> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                it.dependency.imports << Imports.COLLECTIONS
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singletonList($component)")
                it
            }
        } else if (Types.isSet(type)) {
            Optional<TestNodeExpression> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                it.dependency.imports << Imports.COLLECTIONS
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singleton($component)")
                it
            }
        } else if (Types.isMap(type)) {
            List<Type> keyValueTypes = type.getTypeArguments().filter { it.size() == 2 }.map {
                [it[0], it[1]]
            }.orElse([Types.OBJECT, Types.OBJECT])

            Optional<TestNodeExpression> keyTypeValue = valueFactory.getExpression(keyValueTypes[0])
            Optional<TestNodeExpression> valueTypeValue = valueFactory.getExpression(keyValueTypes[1])
            if (keyTypeValue.present && valueTypeValue.present) {
                TestNodeExpression testNodeExpression = new TestNodeExpression()
                TestNodeExpression keyExpression = keyTypeValue.get()
                TestNodeExpression valueExpression = valueTypeValue.get()
                TestNodeMerger.appendDependencies(testNodeExpression, keyExpression)
                TestNodeMerger.appendDependencies(testNodeExpression, valueExpression)

                testNodeExpression.node = JavaParser.parseExpression("ImmutableMap.of(${keyExpression.node}, ${valueExpression.node})")
                testNodeExpression.dependency.imports << Imports.IMMUTABLE_MAP

                return Optional.of(testNodeExpression)
            } else {
                return Optional.empty()
            }
        } else if (Types.isDate(type)) {
            TestNodeExpression expression = new TestNodeExpression(
                    expr: JavaParser.parseExpression("new java.util.Date(${RandomUtils.nextInt(100_000)})")
            )
            expression.dependency.imports << Imports.DATE
            return Optional.of(expression)
        } else if (Types.isSqlDate(type)) {
            TestNodeExpression expression = new TestNodeExpression(
                    expr: JavaParser.parseExpression("new java.sql.Date(${RandomUtils.nextInt(100_000)})")
            )
            expression.dependency.imports << Imports.SQL_DATE
            return Optional.of(expression)
        } else {
            TestNodeExpression expression = new TestNodeExpression()
            expression.node = new MethodCallExpr("mock", new ClassExpr(type))
            expression.dependency.imports << Imports.STATIC_MOCK
            return Optional.of(expression)
        }
    }

    private Optional<TestNodeExpression> getCollectionComponentValue(ClassOrInterfaceType type) {
        Type componentType = type.getTypeArguments()
                                 .filter { !it.empty }
                                 .map { it.first() }
                                 .orElse(Types.OBJECT)
        Optional<TestNodeExpression> componentValue = valueFactory.getExpression(componentType)
        componentValue
    }

}
