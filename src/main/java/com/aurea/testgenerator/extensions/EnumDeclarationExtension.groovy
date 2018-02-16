package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class EnumDeclarationExtension implements ASTExtension {

    EnumDeclarationExtension() {
        log.debug "Adding EnumDeclaration::accessFirst"
        EnumDeclaration.metaClass.accessFirst() {
            EnumDeclaration enumDeclaration = delegate as EnumDeclaration
            NodeList<EnumConstantDeclaration> entries = enumDeclaration.entries
            if (!entries) {
                return Optional.empty()
            }

            String firstEnumName = entries.first().nameAsString
            return Optional.of(new FieldAccessExpr(new NameExpr(enumDeclaration.name), firstEnumName))
        }
    }
}