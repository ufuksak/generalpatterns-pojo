package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.value.Resolution
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
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
        getGetters(classDeclaration) ||
                tryGetToStringMethod(classDeclaration).present ||
                tryGetEqualsMethod(classDeclaration).present ||
                tryGetHashCodeMethod(classDeclaration).present ||
                classDeclaration.constructors ||
                getSetters(classDeclaration)
    }

    static Optional<MethodDeclaration> tryGetToStringMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static Optional<MethodDeclaration> tryGetEqualsMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'equals' && it.type.toString() == 'boolean' && it.public &&
                    it.parameters.size() == 1 && it.parameters.first().type.toString() == 'Object'
        }
    }

    static Optional<MethodDeclaration> tryGetHashCodeMethod(ClassOrInterfaceDeclaration classDeclaration) {
        StreamEx.of(classDeclaration.methods).findFirst {
            it.nameAsString == 'hashCode' && it.type.toString() == 'int' && it.public && !it.parameters
        }
    }

    static List<ResolvedMethodDeclaration> getGetters(ClassOrInterfaceDeclaration classDeclaration) {
        resolvedFields(classDeclaration)
                .collect { PojoMethodsFinder.findGetterMethod(it) }
                .findAll { it.present }
                *.get()
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
        resolvedMethod.accessSpecifier() != AccessSpecifier.PRIVATE && isGetterImplementation(resolvedMethod)
    }

    static boolean isSetterImplementation(ResolvedMethodDeclaration resolvedMethod) {
        if (!(resolvedMethod instanceof JavaParserMethodDeclaration)) {
            return true
        }

        MethodDeclaration methodDeclaration = resolvedMethod.wrappedNode
        methodDeclaration.body.present && simplyAssignsValue(methodDeclaration.body.get())
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
        statement.expressionStmt && statement.asExpressionStmt().expression.assignExpr
    }

    private static boolean isGetterImplementation(ResolvedMethodDeclaration rmd) {
        if (!(rmd instanceof JavaParserMethodDeclaration)) {
            return true
        }

        MethodDeclaration methodDeclaration = rmd.wrappedNode
        methodDeclaration.body.present && simplyReturnsFieldValue(methodDeclaration.body.get())
    }

    private static boolean simplyReturnsFieldValue(BlockStmt block) {
        block.statements.size() == 1 && block.statements.first().returnStmt
    }
}
