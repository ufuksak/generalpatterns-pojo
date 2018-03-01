package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.utils.Pair

class ArbitraryReferenceTypeFactory implements ReferenceTypeFactory {

    ValueFactory valueFactory
    ArbitraryPrimitiveValuesFactory arbitraryPrimitiveValuesFactory = new ArbitraryPrimitiveValuesFactory()

    @Override
    Optional<DependableNode<Expression>> get(ResolvedReferenceType type) {
        if (Types.isBoxedPrimitive(type)) {
            ResolvedPrimitiveType resolvedPrimitiveType = Types.unbox(type)
            return Optional.of(arbitraryPrimitiveValuesFactory.get(resolvedPrimitiveType))
        }
        if (Types.isString(type)) {
            return Optional.of(DependableNode.from(new StringLiteralExpr("ABC")))
        }
        if (Types.isList(type) || Types.isCollection(type) || Types.isIterable(type)) {
            Optional<DependableNode<Expression>> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singletonList($component)")
                it.dependency.imports << Imports.COLLECTIONS
                it
            }
        }
        if (Types.isSet(type)) {
            Optional<DependableNode<Expression>> componentValue = getCollectionComponentValue(type)
            return componentValue.map {
                Expression component = it.node
                it.node = JavaParser.parseExpression("Collections.singleton($component)")
                it.dependency.imports << Imports.COLLECTIONS
                it
            }
        }
        if (Types.isMap(type)) {
            List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParameters = type.getTypeParametersMap()
            ResolvedType keyType
            ResolvedType valueType
            if (typeParameters.size() != 2) {
                keyType = Resolution.tryResolve(Types.OBJECT).get()
                valueType = Resolution.tryResolve(Types.OBJECT).get()
            } else {
                keyType = typeParameters[0].b
                valueType = typeParameters[1].b
            }

            Optional<DependableNode<? extends Expression>> keyTypeValue = valueFactory.getExpression(keyType)
            Optional<DependableNode<? extends Expression>> valueTypeValue = valueFactory.getExpression(valueType)
            if (keyTypeValue.present && valueTypeValue.present) {
                DependableNode<Expression> testNode = new DependableNode<Expression>()
                DependableNode<Expression> keyExpression = keyTypeValue.get()
                DependableNode<Expression> valueExpression = valueTypeValue.get()
                TestNodeMerger.appendDependencies(testNode, keyExpression)
                TestNodeMerger.appendDependencies(testNode, valueExpression)

                testNode.node = JavaParser.parseExpression("ImmutableMap.of(${keyExpression.node}, ${valueExpression.node})")
                testNode.dependency.imports << Imports.IMMUTABLE_MAP

                return Optional.of(testNode)
            } else {
                return Optional.empty()
            }
        }
        if (Types.isDate(type)) {
            DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression("new java.util.Date(42)"))
            expression.dependency.imports << Imports.DATE
            return Optional.of(expression)
        }
        if (Types.isSqlDate(type)) {
            DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression("new java.sql.Date(42)"))
            expression.dependency.imports << Imports.SQL_DATE
            return Optional.of(expression)
        }
        if (Types.isException(type) || Types.isThrowable(type)) {
            DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression('new Exception("Test exception")'))
            return Optional.of(expression)
        }
        if (Types.isRuntimeException(type)) {
            DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression('new RuntimeException("Test exception")'))
            return Optional.of(expression)
        }
        if (Types.isLocale(type)) {
            DependableNode<Expression> expression = DependableNode.from(JavaParser.parseExpression('Locale.getDefault()'))
            expression.dependency.imports << Imports.LOCALE
            return Optional.of(expression)
        }
        if (Types.isEnumeration(type)) {
            ResolvedEnumDeclaration resolvedEnumDeclaration = type.typeDeclaration.asEnum()
            Optional<FieldAccessExpr> accessFirstEnum = resolvedEnumDeclaration.accessFirst()
            return accessFirstEnum.map { DependableNode.from(it) }
        }

        String className = type.typeDeclaration.className
        return Optional.of(MockExpressionBuilder.build(className))
    }

    private Optional<DependableNode<Expression>> getCollectionComponentValue(ResolvedReferenceType type) {
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParameters = type.getTypeParametersMap()
        if (typeParameters.empty) {
            return Optional.empty()
        }

        ResolvedType componentType = typeParameters.first().b
        return valueFactory.getExpression(componentType)
    }
}
