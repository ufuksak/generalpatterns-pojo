package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.ast.FieldAccessResult
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import groovy.util.logging.Log4j2

@Log4j2
class StaticFieldAccessorBuilder {

    ResolvedFieldDeclaration fieldDeclaration

    StaticFieldAccessorBuilder(ResolvedFieldDeclaration fieldDeclaration) {
        assert fieldDeclaration.static
        this.fieldDeclaration = fieldDeclaration
    }

    FieldAccessResult build() {
        if (fieldDeclaration.accessSpecifier() != AccessSpecifier.PRIVATE && fieldDeclaration.static) {
            return FieldAccessResult.success(buildStaticAccess(fieldDeclaration))
        } else {
            PojoMethodsFinder getterFinder = new PojoMethodsFinder(fieldDeclaration, true)
            Optional<ResolvedMethodDeclaration> getter = getterFinder.tryToFindGetter()
            return getter.map { FieldAccessResult.success(buildStaticAccess(it)) }
                         .orElse(FieldAccessResult.NO_ACCESS)
        }
    }

    static FieldAccessExpr buildStaticAccess(ResolvedFieldDeclaration fieldDeclaration) {
        List<String> parentTypes = [fieldDeclaration.declaringType().name]
        gatherParentTypes(parentTypes, fieldDeclaration.declaringType())

        String staticFieldAccessExpression = parentTypes.reverse().join(".") + "." + fieldDeclaration.name
        JavaParser.parseExpression(staticFieldAccessExpression).asFieldAccessExpr()
    }

    static MethodCallExpr buildStaticAccess(ResolvedMethodDeclaration methodDeclaration) {
        List<String> parentTypes = [methodDeclaration.declaringType().name]
        gatherParentTypes(parentTypes, methodDeclaration.declaringType())

        String staticFieldAccessExpression = parentTypes.reverse().join(".") + "." + methodDeclaration.name + "()"
        JavaParser.parseExpression(staticFieldAccessExpression).asMethodCallExpr()
    }

    static void gatherParentTypes(List<String> parentTypes, ResolvedTypeDeclaration type) {
        try {
            type.containerType().ifPresent {
                parentTypes << it.name
                gatherParentTypes(parentTypes, it)
            }
        } catch (IllegalArgumentException iae) {
            log.debug "Failed to get parent type, but it appears to be a bug in JSS", iae
        }
    }
}
