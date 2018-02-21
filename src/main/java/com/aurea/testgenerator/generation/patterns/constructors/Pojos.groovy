package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.generation.source.PojoFinder
import com.aurea.testgenerator.value.Types
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class Pojos {

    static boolean isPojo(ClassOrInterfaceDeclaration coid) {
        hasAtleastOneGetter(coid) ||
                hasToStringMethod(coid) ||
                hasEquals(coid) ||
                hasHashCode(coid) ||
                hasConstructors(coid) || 
                hasAtLeastOneSetter(coid)
    }

    static boolean hasToStringMethod(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static boolean hasEquals(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'equals' && it.type.toString() == 'boolean' && it.public &&
                    it.parameters.size() == 1 && it.parameters.first().type.toString() == 'Object'
        }
    }

    static boolean hasHashCode(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'hashCode' && it.type.toString() == 'int' && it.public && !it.parameters
        }
    }

    static boolean hasConstructors(ClassOrInterfaceDeclaration coid) {
        coid.constructors
    }

    static boolean hasAtleastOneGetter(ClassOrInterfaceDeclaration coid) {
        resolvedFields(coid).anyMatch { resolvedField ->
            PojoFinder getterFinder = new PojoFinder(resolvedField)
            getterFinder.tryToFindGetter().present
        }
    }

    static boolean hasAtLeastOneSetter(ClassOrInterfaceDeclaration coid) {
        resolvedFields(coid).anyMatch { resolvedField ->
            PojoFinder setterFinder = new PojoFinder(resolvedField)
            setterFinder.tryToFindSetter().present
        }
    }

    private static StreamEx<ResolvedFieldDeclaration> resolvedFields(ClassOrInterfaceDeclaration coid) {
        StreamEx.of(coid.fields)
                .map { Types.tryResolve(it) }
                .filter { it.present }
                .map { it.get() }
    }
}
