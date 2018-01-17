package com.aurea.testgenerator.config

import com.github.javaparser.ast.body.FieldDeclaration
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class FieldExtension implements ASTExtension {

    FieldExtension() {
        log.info "Adding FieldDeclaration::nameAsString"
        FieldDeclaration.metaClass.getNameAsString() {
            (delegate as FieldDeclaration).variables[0].nameAsString
        }
    }
}