package com.aurea.testgenerator.ast

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration


class FieldAccessBuilder {

    final Expression scope

    FieldAccessBuilder(Expression scope) {
        this.scope = scope
    }

    FieldAccessResult build(ResolvedFieldDeclaration field) {
        if (field.static) {
            return new StaticFieldAccessorBuilder(field).build()
        } else {
            return new NonStaticFieldAccessorBuilder(field, scope).build()
        }
    }

}
