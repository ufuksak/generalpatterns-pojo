package com.aurea.testgenerator.extensions

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class TypeDeclarationExtension implements ASTExtension {
    TypeDeclarationExtension() {
        log.debug "Adding TypeDeclaration::isAnonymous"
        TypeDeclaration.metaClass.isAnonymous() {
            TypeDeclaration n = delegate as TypeDeclaration
            ASTNodeUtils.parents(n).anyMatch() { !it instanceof TypeDeclaration }
        }
    }
}
