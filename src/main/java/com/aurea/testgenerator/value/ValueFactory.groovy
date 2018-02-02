package com.aurea.testgenerator.value

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.Type


interface ValueFactory {

    Optional<Expression> get(Type type)
}