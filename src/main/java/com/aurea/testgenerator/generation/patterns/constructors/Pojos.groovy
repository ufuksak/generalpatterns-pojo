package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.generation.source.PojoFinder
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import groovy.util.logging.Log4j2


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
        for (FieldDeclaration field : coid.fields) {
            try {
                ResolvedFieldDeclaration resolvedField = field.resolve()
                PojoFinder getterFinder = new PojoFinder(resolvedField)
                if (getterFinder.tryToFindGetter().present) {
                    return true
                }
            } catch (UnsolvedSymbolException use) {
                log.debug "Failed to solve $field in $coid", use
            }
        }
        return false
    }

    static boolean hasAtLeastOneSetter(ClassOrInterfaceDeclaration coid) {
        for (FieldDeclaration field : coid.fields) {
            try {
                ResolvedFieldDeclaration resolvedField = field.resolve()
                PojoFinder setterFinder = new PojoFinder(resolvedField)
                if (setterFinder.tryToFindSetter().present) {
                    return true
                }
            } catch (UnsolvedSymbolException use) {
                log.debug "Failed to solve $field in $coid", use
            }
        }
        return false
    }
}
