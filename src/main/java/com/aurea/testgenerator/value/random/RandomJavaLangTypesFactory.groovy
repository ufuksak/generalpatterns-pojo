package com.aurea.testgenerator.value.random

import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.value.Types
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type

class RandomJavaLangTypesFactory {

    ValueFactory valueFactory

    RandomJavaLangTypesFactory(ValueFactory valueFactory) {
        this.valueFactory = valueFactory
    }

    Optional<TestNodeExpression> get(ClassOrInterfaceType type) {
        if (type.isBoxedType()) {
            return Optional.of(RandomPrimitiveValueFactory.get(type.toUnboxedType()))
        } else if (Types.isList(type) || Types.isCollection(type) || Types.isIterable(type)) {
            Optional<TestNodeExpression> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                it.imports << Imports.COLLECTIONS
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singletonList($component)")
                it
            }
        } else if (Types.isSet(type)) {
            Optional<TestNodeExpression> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                it.imports << Imports.COLLECTIONS
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singleton($component)")
                it
            }
        } else if (Types.isMap(type)) {
            List<Type> keyValueTypes = type.getTypeArguments().filter {it.size() == 2 }.map {
                [it[0], it[1]]
            }.orElse([Types.OBJECT, Types.OBJECT])

            Optional<TestNodeExpression> keyTypeValue = valueFactory.get(keyValueTypes[0])
            Optional<TestNodeExpression> valueTypeValue = valueFactory.get(keyValueTypes[1])
            if (keyTypeValue.present && valueTypeValue.present) {
                TestNodeExpression testNodeExpression = new TestNodeExpression()
                TestNodeExpression keyExpression = keyTypeValue.get()
                TestNodeExpression valueExpression = valueTypeValue.get()
                testNodeExpression.imports.addAll(keyExpression.imports)
                testNodeExpression.imports.addAll(valueExpression.imports)
                Statement

//                testNodeExpression.assignBlock
//

                return Optional.of(testNodeExpression)
            } else {
                return Optional.empty()
            }
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
