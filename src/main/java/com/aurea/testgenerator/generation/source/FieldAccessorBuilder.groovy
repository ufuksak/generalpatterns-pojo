package com.aurea.testgenerator.generation.source

import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.MethodUsage
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class FieldAccessorBuilder {

    ResolvedFieldDeclaration fieldDeclaration
    Expression scope

    FieldAccessorBuilder(ResolvedFieldDeclaration fieldDeclaration, Expression scope) {
        assert !fieldDeclaration.static
        this.fieldDeclaration = fieldDeclaration
        this.scope = scope
    }

    Optional<Expression> build() {
        if (fieldDeclaration.accessSpecifier() != AccessSpecifier.PRIVATE) {
            return Optional.ofNullable(new FieldAccessExpr(scope, fieldDeclaration.name))
        } else {
            Optional<MethodUsage> getter = tryToFindGetter()

        }
        return Optional.empty()
    }

    Optional<MethodUsage> tryToFindGetter() {
        String expectedGetterName = "get" + fieldDeclaration.name.capitalize()
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class) {
            return StreamEx.of(rtd.asClass().allMethods).findFirst {
                it.name == expectedGetterName && isGetter(it)
            }
        }
    }

    private boolean isGetter(MethodUsage mu) {
        mu.declaration.accessSpecifier() != AccessSpecifier.PRIVATE &&
                mu.declaration.returnType == fieldDeclaration.type &&
                checkSizeForJavaParserDeclaration(mu)
    }

    private boolean checkSizeForJavaParserDeclaration(MethodUsage mu) {
        if (mu.declaration instanceof JavaParserMethodDeclaration) {
            MethodDeclaration md = (mu.declaration as JavaParserMethodDeclaration).wrappedNode
            md.body.present && simplyReturnsFieldValue(md.body.get())
        }
        true
    }

    private boolean simplyReturnsFieldValue(BlockStmt block) {
        block.statements.size() == 1 && isReturnFieldValueStatement(block.statements.first())
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
}

