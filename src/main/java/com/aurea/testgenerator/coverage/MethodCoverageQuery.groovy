package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.ObjectCreationExpr
import one.util.streamex.StreamEx

import static com.google.common.base.Preconditions.checkNotNull

class MethodCoverageQuery {
    Unit unit
    TypeDeclaration type
    CallableDeclaration method
    int anonymousClassIndex

    private MethodCoverageQuery(Unit unit, TypeDeclaration type, CallableDeclaration methodDeclaration, int anonymousClassIndex) {
        this.unit = unit
        this.type = type
        this.method = methodDeclaration
        this.anonymousClassIndex = anonymousClassIndex
    }

    static MethodCoverageQuery of(Unit unit, TypeDeclaration type, CallableDeclaration method) {
        checkNotNull(unit, "%s must not be null", "Unit")
        checkNotNull(type, "%s must not be null", "Type")
        checkNotNull(method, "%s must not be null", "Method")
        return new MethodCoverageQuery(unit, type, method, 0)
    }

    static MethodCoverageQuery of(Unit unit, CallableDeclaration method) {
        ObjectCreationExpr objectCreationExpr = ASTNodeUtils.findParentOf(ObjectCreationExpr.class, method)
        if (null != objectCreationExpr) {
            TypeDeclaration classOrInterfaceDeclaration = ASTNodeUtils.findParentSubTypeOf(TypeDeclaration.class, method)
            List<ObjectCreationExpr> objectCreationExprs = ASTNodeUtils.findChildsOf(ObjectCreationExpr.class, classOrInterfaceDeclaration)
            List<ObjectCreationExpr> filtered = StreamEx.of(objectCreationExprs).filter { it.getAnonymousClassBody() != null }.toList()
            int anonymousClassIndex = filtered.indexOf(objectCreationExpr) + 1
            return new MethodCoverageQuery(unit, classOrInterfaceDeclaration, method, anonymousClassIndex)
        }
        return of(unit, ASTNodeUtils.findParentSubTypeOf(TypeDeclaration.class, method), method)
    }

    @Override
    String toString() {
        unit.fullName + "::" + method.getName()
    }
}