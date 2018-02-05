package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.body.FieldDeclaration
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class FieldDeclarationExtension implements ASTExtension {

    FieldDeclarationExtension() {
        log.debug "Adding FieldDeclaration::nameAsString"
        FieldDeclaration.metaClass.getNameAsString() {
            (delegate as FieldDeclaration).variables[0].nameAsString
        }
        FieldDeclaration.metaClass.getType() {
            (delegate as FieldDeclaration).variables[0].type
        }
        FieldDeclaration.metaClass.getVariableByName() { String name ->
            Optional.ofNullable((delegate as FieldDeclaration).variables.find { it.nameAsString == name })
        }
    }
}