package com.aurea.testgenerator.ast

import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.nodeTypes.NodeWithBlockStmt
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import one.util.streamex.StreamEx

import java.util.function.BinaryOperator

class FieldAssignmentsVisitor {

    static Collection<AssignExpr> findLastAssignExpressionsByField(List<AssignExpr> exprs) {
        Map<SimpleName, AssignExpr> mapByName = StreamEx.of(exprs)
                                                        .filter { it.target.fieldAccessExpr }
                                                        .toMap(
                { it.target.asFieldAccessExpr().name },
                { it },
                { ae1, ae2 -> ae2 } as BinaryOperator<AssignExpr>)

        mapByName.values()
    }

    Collection<FieldAssignment> visit(ConstructorDeclaration constructor) {
        List<FieldAssignment> result = constructor.body.findAll(AssignExpr)
    }

    Collection<AssignExpr> findArgumentAssignExpressions(ConstructorDeclaration cd) {
        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        cd.body.findFirst(ExplicitConstructorInvocationStmt).map { explicitConstructorInvocationStmt ->
            if (explicitConstructorInvocationStmt.this) {
                getConstructorByParameters(explicitConstructorInvocationStmt).ifPresent {
                    assignExprs.addAll findArgumentAssignExpressions(it)
                }
            } else {
                getSuperConstructor(explicitConstructorInvocationStmt).ifPresent {
                    assignExprs.addAll findArgumentAssignExpressions(it)
                }
            }
        }
        Collection<AssignExpr> onlyLastAssignExprs = findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> onlyArgumentAssignExprs = onlyLastAssignExprs.findAll {
            it.value.nameExpr && cd.isNameOfArgument(it.value.asNameExpr().name)
        }
        onlyArgumentAssignExprs
    }

    private static Optional<ConstructorDeclaration> getConstructorByParameters(ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt) {
        TypeDeclaration declaredType = explicitConstructorInvocationStmt.getAncestorOfType(TypeDeclaration).get()
        List<ConstructorDeclaration> constructors = ASTNodeUtils.findDirectChildsOf(ConstructorDeclaration, declaredType)
        StreamEx.of(constructors).findFirst { constructor ->
            Callability.canBeCalledWithArguments(constructor, explicitConstructorInvocationStmt.arguments)
        }
    }

    private Optional<ConstructorDeclaration> getSuperConstructor(ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt) {
        Optional.empty()
    }

    private static Collection<AssignExpr> findLiteralAssignmentExpressions(ConstructorDeclaration cd) {
        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = FieldAssignmentsVisitor.findLastAssignExpressionsByField(assignExprs)
        onlyLastAssignExprs.findAll { it.value.literalExpr }
    }

}
