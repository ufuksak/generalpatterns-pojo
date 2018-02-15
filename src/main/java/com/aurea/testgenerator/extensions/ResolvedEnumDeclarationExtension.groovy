package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class ResolvedEnumDeclarationExtension implements ASTExtension {

    ResolvedEnumDeclarationExtension() {
        log.debug "Adding EnumDeclaration::accessFirst"
        ResolvedEnumDeclaration.metaClass.accessFirst() {
            ResolvedEnumDeclaration enumDeclaration = delegate as ResolvedEnumDeclaration
            Collection<ResolvedEnumConstantDeclaration> entries = enumDeclaration.enumConstants
            if (!entries) {
                return Optional.empty()
            } 
            String firstEnumName = entries.first().name
            return Optional.of(new FieldAccessExpr(new NameExpr(enumDeclaration.className), firstEnumName))
        }
    }
}