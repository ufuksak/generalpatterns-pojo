package com.aurea.testgenerator.generation.mock.util

import com.aurea.testgenerator.value.Types
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.type.Type

class MockitoUtils {
    static String getArgs(MethodDeclaration method, MethodCallExpr delegateExpression) {
        Map<String, Type> methodParamToType = method.parameters.collectEntries { [it.nameAsString, it.type] }
        delegateExpression.arguments.collect {
            Type argType = methodParamToType.get(it.toString())
            if (argType?.isClassOrInterfaceType()
                    && !Types.isString(argType)
                    && !Types.isBoxedPrimitive(argType)) {
                "any(${argType.asString()}.class)"
            } else {
                "eq(${it})"
            }
        }.join(",")
    }
}
