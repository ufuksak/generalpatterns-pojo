package com.aurea.testgenerator.generation.source

import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import one.util.streamex.StreamEx


class PojoFinder {

    ResolvedFieldDeclaration fieldDeclaration
    boolean isStatic

    PojoFinder(ResolvedFieldDeclaration fieldDeclaration, boolean isStatic = false) {
        this.fieldDeclaration = fieldDeclaration
        this.isStatic = isStatic
    }

    Optional<ResolvedMethodDeclaration> tryToFindGetter() {
        String expectedGetterName = "get" + fieldDeclaration.name.capitalize()
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == expectedGetterName && isGetter(it)
            }
        } else if (rtd.enum) {
            //TODO: Add enum support
        }
        return Optional.empty()
    }

    Optional<ResolvedMethodDeclaration> tryToFindSetter() {
        String expectedSetterName = "set" + fieldDeclaration.name.capitalize()
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == expectedSetterName && isSetter(it)
            }
        } else if (rtd.enum) {
            //TODO: Add enum support
        }
        return Optional.empty()
    }

    private boolean isGetter(ResolvedMethodDeclaration rmd) {
        rmd.accessSpecifier() != AccessSpecifier.PRIVATE &&
                (isStatic ? rmd.isStatic() : !rmd.isStatic()) &&
                rmd.returnType == fieldDeclaration.getType() &&
                isGetterImplementation(rmd)
    }

    private boolean isSetter(ResolvedMethodDeclaration rmd) {
        rmd.accessSpecifier() != AccessSpecifier.PRIVATE &&
                (isStatic ? rmd.isStatic() : !rmd.isStatic()) &&
                rmd.returnType.isVoid() &&
                rmd.getNumberOfParams() == 1 ||
                rmd.getParam(0).type == fieldDeclaration.type &&
                isSetterImplementation(rmd)
    }

    private boolean isGetterImplementation(ResolvedMethodDeclaration rmd) {
        if (rmd instanceof JavaParserMethodDeclaration) {
            MethodDeclaration md = (rmd as JavaParserMethodDeclaration).wrappedNode
            md.body.present && simplyReturnsFieldValue(md.body.get())
        }
        true
    }

    private boolean isSetterImplementation(ResolvedMethodDeclaration rmd) {
        if (rmd instanceof JavaParserMethodDeclaration) {
            MethodDeclaration md = (rmd as JavaParserMethodDeclaration).wrappedNode
            md.body.present && simplyAssignsValue(md.body.get())
        }
        true
    }

    private boolean simplyReturnsFieldValue(BlockStmt block) {
        block.statements.size() == 1 && isReturnFieldValueStatement(block.statements.first())
    }

    private boolean simplyAssignsValue(BlockStmt block) {
        block.statements.size() == 1 && isAssignExpr(block.statements.first())
    }

    private boolean isReturnFieldValueStatement(Statement statement) {
        if (statement.returnStmt) {
            Optional<Expression> maybeReturnExpression = statement.asReturnStmt().expression
            maybeReturnExpression.map { returnExpression ->
                if (returnExpression.nameExpr) {
                    returnExpression.asNameExpr().nameAsString == fieldDeclaration.name
                } else if (returnExpression.fieldAccessExpr) {
                    FieldAccessExpr fae = returnExpression.asFieldAccessExpr()
                    fae.scope.thisExpr && fae.nameAsString == fieldDeclaration.name
                }
            }.orElse(false)
        }
        false
    }

    private boolean isAssignExpr(Statement statement) {
        if (statement.expressionStmt) {
            Expression expr = statement.asExpressionStmt().expression
            return expr.assignExpr
        }
        return false
    }
}
