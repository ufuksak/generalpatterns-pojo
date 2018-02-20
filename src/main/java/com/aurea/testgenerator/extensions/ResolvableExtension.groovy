package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.resolution.Resolvable
import groovy.util.logging.Log4j2
import org.reflections.Reflections
import org.springframework.stereotype.Component


@Component
@Log4j2
class ResolvableExtension implements ASTExtension {

    ResolvableExtension() {
        log.debug "Adding FieldDeclaration::tryResolve"
        Reflections reflections = new Reflections("com.github.javaparser.ast")
        reflections.getSubTypesOf(Resolvable).each {
            log.debug "Add ${it.class}::tryResolve"
            it.metaClass.tryResolve() {
                Resolvable r = (delegate as Resolvable)
                try {
                    return Optional.ofNullable(r.resolve())
                } catch (Exception e) {
                    return Optional.empty()
                }
            }
        }
    }
}
