package com.aurea.testgenerator.config

import com.github.javaparser.ast.body.VariableDeclarator
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class VariableDeclaratorExtension implements ASTExtension {
    VariableDeclaratorExtension() {
        log.info "Adding VariableDeclarator::getInitializerText"
        VariableDeclarator.metaClass.getInitializerText() {
            VariableDeclarator n = delegate as VariableDeclarator
            n.initializer.map {
                it.toString()
            }.orElse("")
        }
    }
}
