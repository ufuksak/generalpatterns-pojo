package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.expr.SimpleName
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class ConstructorDeclarationExtension implements ASTExtension {

    ConstructorDeclarationExtension() {
        log.debug "Adding ConstructorDeclaration::isNameOfArgument"
        ConstructorDeclaration.metaClass.isNameOfArgument() { SimpleName name ->
            (delegate as ConstructorDeclaration).parameters.any { it.name == name }
        }
    }
}
