package com.aurea.testgenerator.pattern.general

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr

import java.util.function.Predicate


class ObjectCreationExprPureFunctionPredicate implements Predicate<ObjectCreationExpr> {
    @Override
    boolean test(ObjectCreationExpr expr) {
        MethodDeclaration md = expr.getAncestorOfType(MethodDeclaration)
                                   .orElseThrow {
            new IllegalStateException("${this.class.name} must be called on " +
                    "an expression in a method body")
        }
        List<String> parameterNames = md.parameters.collect { it.nameAsString }
        boolean isAssignment = expr.parentNode.map { it instanceof AssignExpr}.orElse(false)

        boolean isInputParameter = (expr.name as NameExpr).nameAsString in parameterNames

        !(isInputParameter && isAssignment)
    }
}
