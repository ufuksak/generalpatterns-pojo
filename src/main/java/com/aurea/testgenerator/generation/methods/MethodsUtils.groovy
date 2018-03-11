package com.aurea.testgenerator.generation.methods

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.MethodCallExpr

class MethodsUtils {
    static boolean doNotReassignParameters(MethodDeclaration methodDeclaration) {
        List<String> paramNames = getParamNames(methodDeclaration)
        if (paramNames.isEmpty()) {
            return true
        }
        List<AssignExpr> assignExprs = methodDeclaration.findAll(AssignExpr)
        List<String> assignedNames = assignExprs.collect { it.target.asNameExpr().nameAsString }
        return assignedNames.disjoint(paramNames)
    }

    static Optional<MethodCallExpr> findLastMethodCallExpression (MethodDeclaration methodDeclaration){
        List<MethodCallExpr> expressions = methodDeclaration.findAll(MethodCallExpr).findAll {
            !(it.parentNode.isPresent() && it.parentNode.get() instanceof MethodCallExpr)
        }
        if(!expressions.isEmpty()){
            return Optional.of(expressions.last())
        }
        return Optional.empty()
    }

    static List<String> getParamNames(MethodDeclaration methodDeclaration) {
        List<String> paramNames = methodDeclaration.parameters.collect { it.nameAsString }
        paramNames
    }

    static boolean returnsClassOrInterface(MethodDeclaration method) {
        method.type && method.type.isClassOrInterfaceType()
    }
}
