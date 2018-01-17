package com.aurea.testgenerator.config

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

@Component
@Log4j2
class ClassOrInterfaceExtension implements ASTExtension {
    ClassOrInterfaceExtension() {
        log.info "Adding ClassOrInterfaceExtension::findVariableByName"
        ClassOrInterfaceDeclaration.metaClass.findVariableByName() { String variableName ->
            ClassOrInterfaceDeclaration n = delegate as ClassOrInterfaceDeclaration
            StreamEx.of(n.getNodesByType(VariableDeclarator))
                    .findFirst { it.nameAsString == variableName }.orElse(null)
        }

        log.info "Adding ClassOrInterfaceExtension::getFullName"
        ClassOrInterfaceDeclaration.metaClass.getFullName() {
            ClassOrInterfaceDeclaration n = delegate as ClassOrInterfaceDeclaration
            ASTNodeUtils.getFullName(n)
        }
    }
}
