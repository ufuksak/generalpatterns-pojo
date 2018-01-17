package com.aurea.testgenerator.pattern.general

import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.UnaryExpr

import javax.lang.model.type.PrimitiveType
import java.util.function.BiPredicate


class UnaryExprPureFunctionPredicate implements BiPredicate<UnaryExpr, MethodContext> {
    @Override
    boolean test(UnaryExpr unaryExpr, MethodContext context) {
        Expression target = unaryExpr.expression
        if (target instanceof NameExpr) {
            Optional<Parameter> parameter = context.getMethodParameterByName(target.nameAsString)
            boolean mutatesInputParameter = parameter.map {
                if (it.type instanceof PrimitiveType) {
                    true
                }
            }.orElse(false)
            Optional<VariableDeclarator> variableDeclarator = context.getClassVariableByName(target.nameAsString)
            boolean mutatesClassField = variableDeclarator.present
            return !(mutatesInputParameter || mutatesClassField)
        }
        return false
    }
}
