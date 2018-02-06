package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import groovy.util.logging.Log4j2

@Log4j2
class StaticFieldAccessorBuilder {

    ResolvedFieldDeclaration fieldDeclaration

    StaticFieldAccessorBuilder(ResolvedFieldDeclaration fieldDeclaration) {
        assert fieldDeclaration.static
        this.fieldDeclaration = fieldDeclaration
    }

    Optional<Expression> build() {
        if (fieldDeclaration.accessSpecifier() != AccessSpecifier.PRIVATE && fieldDeclaration.static) {
            return Optional.of(buildStaticAccess(fieldDeclaration))
        } else {
            return Optional.empty()
        }
    }

    static FieldAccessExpr buildStaticAccess(ResolvedFieldDeclaration fieldDeclaration) {
        List<String> parentTypes = [fieldDeclaration.declaringType().name]
        gatherParentTypes(parentTypes, fieldDeclaration.declaringType())

        String staticFieldAccessExpression = parentTypes.reverse().join(".") + "." + fieldDeclaration.name
        JavaParser.parseExpression(staticFieldAccessExpression).asFieldAccessExpr()
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
