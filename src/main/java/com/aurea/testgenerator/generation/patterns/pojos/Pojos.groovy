package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.jasongoodwin.monads.Try
import groovy.transform.Memoized
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class Pojos {

    static boolean isPojo(ClassOrInterfaceDeclaration classDeclaration) {
        hasAtleastOneGetter(classDeclaration) ||
                hasToStringMethod(classDeclaration) ||
                hasEquals(classDeclaration) ||
                hasHashCode(classDeclaration) ||
                hasConstructors(classDeclaration) ||
                hasAtLeastOneSetter(classDeclaration)
    }

    static boolean hasToStringMethod(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.methods.any {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static Optional<MethodDeclaration> tryGetToStringMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static boolean hasEquals(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.methods.any {
            it.nameAsString == 'equals' && it.type.toString() == 'boolean' && it.public &&
                    it.parameters.size() == 1 && it.parameters.first().type.toString() == 'Object'
        }
    }

    static Optional<MethodDeclaration> tryGetEqualsMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'equals' && it.type.toString() == 'boolean' && it.public &&
                    it.parameters.size() == 1 && it.parameters.first().type.toString() == 'Object'
        }
    }

    static boolean hasHashCode(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.methods.any {
            it.nameAsString == 'hashCode' && it.type.toString() == 'int' && it.public && !it.parameters
        }
    }

    static Optional<MethodDeclaration> tryGetHashCodeMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'hashCode' && it.type.toString() == 'int' && it.public && !it.parameters
        }
    }

    static boolean hasConstructors(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.constructors
    }

    static boolean hasAtleastOneGetter(ClassOrInterfaceDeclaration classDeclaration) {
        resolvedFields(classDeclaration).any { PojoMethodsFinder.findGetterMethod(it).present }
    }

    static List<ResolvedMethodDeclaration> getGetters(ClassOrInterfaceDeclaration classDeclaration) {
        resolvedFields(classDeclaration)
                .collect { PojoMethodsFinder.findGetterMethod(it) }
                .findAll { it.present }
                *.get()
    }

    static boolean hasAtLeastOneSetter(ClassOrInterfaceDeclaration classDeclaration) {
        resolvedFields(classDeclaration).any { PojoMethodsFinder.findSetterMethod(it).present }
    }

    static List<ResolvedMethodDeclaration> getSetters(ClassOrInterfaceDeclaration classDeclaration) {
        resolvedFields(classDeclaration)
                .collect() { PojoMethodsFinder.findSetterMethod(it) }
                .findAll { it.present }
                *.get()
    }

    @Memoized
    private static List<ResolvedFieldDeclaration> resolvedFields(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.fields
                .collect { Resolution.tryResolve(it) }
                .findAll { it.present }
                *.get()
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
        Resolution.tryResolve(methodCall)
                .map { isSetterSignature(it) }
                .orElse(false)
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
