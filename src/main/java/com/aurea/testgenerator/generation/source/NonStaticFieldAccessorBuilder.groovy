package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.ast.FieldAccessResult
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import groovy.util.logging.Log4j2

@Log4j2
class NonStaticFieldAccessorBuilder {

    ResolvedFieldDeclaration fieldDeclaration
    Expression scope

    NonStaticFieldAccessorBuilder(ResolvedFieldDeclaration fieldDeclaration, Expression scope) {
        assert !fieldDeclaration.static
        this.fieldDeclaration = fieldDeclaration
        this.scope = scope
    }

    FieldAccessResult build() {
        if (fieldDeclaration.accessSpecifier() != AccessSpecifier.PRIVATE) {
            return FieldAccessResult.success(new FieldAccessExpr(scope, fieldDeclaration.name))
        } else {
            PojoMethodsFinder getterFinder = new PojoMethodsFinder(fieldDeclaration)
            Optional<ResolvedMethodDeclaration> getter = getterFinder.tryToFindGetter()
            return getter.map { FieldAccessResult.success(new MethodCallExpr(scope, it.name)) }
                         .orElse(FieldAccessResult.NO_ACCESS)
        }
    }
}

