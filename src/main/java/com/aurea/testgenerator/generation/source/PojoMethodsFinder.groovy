package com.aurea.testgenerator.generation.source

import com.aurea.testgenerator.generation.patterns.constructors.Pojos
import com.aurea.testgenerator.value.Types
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import one.util.streamex.StreamEx

class PojoMethodsFinder {

    ResolvedFieldDeclaration fieldDeclaration
    boolean isStatic

    PojoMethodsFinder(ResolvedFieldDeclaration fieldDeclaration, boolean isStatic = false) {
        this.fieldDeclaration = fieldDeclaration
        this.isStatic = isStatic
    }

    Optional<ResolvedMethodDeclaration> tryToFindGetter() {
        try {
            if (Types.isBooleanType(fieldDeclaration.getType())) {
                String expectedGetterName = 'is' + fieldDeclaration.name.capitalize()
                if (expectedGetterName.startsWith('isIs')) {
                    expectedGetterName = 'is' + expectedGetterName.substring('isIs'.length())
                }
                Optional<ResolvedMethodDeclaration> withIsName = findGetterWithName(expectedGetterName)
                if (withIsName.present) {
                    return withIsName
                }
            }
            return findGetterWithName('get' + fieldDeclaration.name.capitalize())
        } catch (Exception e) {
            return Optional.empty()
        }
    }

    Optional<ResolvedMethodDeclaration> findGetterWithName(String expectedName) {
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == expectedName && isGetter(it)
            }
        }
        return Optional.empty()
    }

    Optional<ResolvedMethodDeclaration> tryToFindSetter() {
        try {
            if (Types.isBooleanType(fieldDeclaration.getType())) {
                String fieldName = fieldDeclaration.name
                if (fieldName.startsWith('is')) {
                    String expectedName = 'set' + fieldName.substring('is'.length())
                    Optional<ResolvedMethodDeclaration> withIsName = findSetterWithName(expectedName)
                    if (withIsName.present) {
                        return withIsName
                    }
                }
            }
            return findSetterWithName('set' + fieldDeclaration.name.capitalize())
        } catch (Exception e) {
            return Optional.empty()
        }
    }

    Optional<ResolvedMethodDeclaration> findSetterWithName(String name) {
        ResolvedTypeDeclaration rtd = fieldDeclaration.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().declaredMethods).findFirst {
                it.name == name && isSetter(it)
            }
        } else if (rtd.enum) {
            //TODO: Add enum support
        }
        return Optional.empty()
    }

    private boolean isGetter(ResolvedMethodDeclaration rmd) {
        (isStatic ? rmd.isStatic() : !rmd.isStatic()) &&
                Pojos.isGetterSignature(rmd) &&
                Types.areSameOrBoxedSame(rmd.returnType, fieldDeclaration.getType())
    }

    private boolean isSetter(ResolvedMethodDeclaration rmd) {
        (isStatic ? rmd.isStatic() : !rmd.isStatic()) &&
                Pojos.isSetterSignature(rmd) &&
                Types.areSameOrBoxedSame(rmd.getParam(0).getType(), fieldDeclaration.getType())
    }
}
