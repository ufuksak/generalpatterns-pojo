package com.aurea.testgenerator.generation.methods

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.jasongoodwin.monads.Try

class MethodsUtils {
    static boolean doNotReassignParameters(MethodDeclaration methodDeclaration) {
        getParamNames(methodDeclaration)
                .disjoint( methodDeclaration.findAll(AssignExpr).collect { it.target.asNameExpr().nameAsString })
    }

    static Optional<MethodCallExpr> findLastMethodCallExpression (MethodDeclaration methodDeclaration){
        List<MethodCallExpr> expressions = methodDeclaration.findAll(MethodCallExpr).findAll {
            !(it.parentNode.isPresent() && it.parentNode.get() instanceof MethodCallExpr)
        }
        Optional.ofNullable(Try.ofFailable{expressions.last()}.orElse(null))
    }

    static List<String> getParamNames(MethodDeclaration methodDeclaration) {
        methodDeclaration.parameters.collect { it.nameAsString }
    }

    static boolean returnsClassOrInterface(MethodDeclaration method) {
        method.type && method.type.isClassOrInterfaceType()
    }
}
