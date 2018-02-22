package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.generation.source.PojoMethodsFinder
import com.aurea.testgenerator.value.Types
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.jasongoodwin.monads.Try
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class Pojos {

    static boolean isPojo(ClassOrInterfaceDeclaration coid) {
        hasAtleastOneGetter(coid) ||
                hasToStringMethod(coid) ||
                hasEquals(coid) ||
                hasHashCode(coid) ||
                hasConstructors(coid) || 
                hasAtLeastOneSetter(coid)
    }

    static boolean hasToStringMethod(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static boolean hasEquals(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'equals' && it.type.toString() == 'boolean' && it.public &&
                    it.parameters.size() == 1 && it.parameters.first().type.toString() == 'Object'
        }
    }

    static boolean hasHashCode(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'hashCode' && it.type.toString() == 'int' && it.public && !it.parameters
        }
    }

    static boolean hasConstructors(ClassOrInterfaceDeclaration coid) {
        coid.constructors
    }

    static boolean hasAtleastOneGetter(ClassOrInterfaceDeclaration coid) {
        resolvedFields(coid).anyMatch { resolvedField ->
            PojoMethodsFinder getterFinder = new PojoMethodsFinder(resolvedField)
            getterFinder.tryToFindGetter().present
        }
    }

    static boolean hasAtLeastOneSetter(ClassOrInterfaceDeclaration coid) {
        resolvedFields(coid).anyMatch { resolvedField ->
            PojoMethodsFinder setterFinder = new PojoMethodsFinder(resolvedField)
            setterFinder.tryToFindSetter().present
        }
    }

    private static StreamEx<ResolvedFieldDeclaration> resolvedFields(ClassOrInterfaceDeclaration coid) {
        StreamEx.of(coid.fields)
                .map { Types.tryResolve(it) }
                .filter { it.present }
                .map { it.get() }
    }

    static boolean isSetterSignature(ResolvedMethodDeclaration resolvedMethod) {
        Try.ofFailable {
            resolvedMethod.accessSpecifier() != AccessSpecifier.PRIVATE &&
                    resolvedMethod.returnType.isVoid() &&
                    resolvedMethod.getNumberOfParams() == 1 &&
                    isSetterImplementation(resolvedMethod)
        }.orElse(false)
    }

    static boolean isGetterSignature(ResolvedMethodDeclaration resolvedMethod) {
        resolvedMethod.accessSpecifier() != AccessSpecifier.PRIVATE &&
                isGetterImplementation(resolvedMethod)
    }

    static boolean isSetterImplementation(ResolvedMethodDeclaration resolvedMethod) {
        if (resolvedMethod instanceof JavaParserMethodDeclaration) {
            MethodDeclaration md = (resolvedMethod as JavaParserMethodDeclaration).wrappedNode
            md.body.present && simplyAssignsValue(md.body.get())
        }
        true
    }

    static boolean isSetterCall(MethodCallExpr methodCall) {
        Types.tryResolve(methodCall).map {
            isSetterSignature(it)
        }.orElse(false)
    }

    private static boolean simplyAssignsValue(BlockStmt block) {
        block.statements.size() == 1 && isAssignExpr(block.statements.first())
    }

    private static boolean isAssignExpr(Statement statement) {
        if (statement.expressionStmt) {
            Expression expr = statement.asExpressionStmt().expression
            return expr.assignExpr
        }
        return false
    }

    private static boolean isGetterImplementation(ResolvedMethodDeclaration rmd) {
        if (rmd instanceof JavaParserMethodDeclaration) {
            MethodDeclaration md = (rmd as JavaParserMethodDeclaration).wrappedNode
            md.body.present && simplyReturnsFieldValue(md.body.get())
        }
        true
    }

    private static boolean simplyReturnsFieldValue(BlockStmt block) {
        block.statements.size() == 1 && block.statements.first().returnStmt
    }

}
