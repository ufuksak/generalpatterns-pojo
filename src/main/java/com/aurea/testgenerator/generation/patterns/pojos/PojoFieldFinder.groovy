package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.value.Types
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import groovy.transform.Canonical
import groovy.transform.InheritConstructors
import one.util.streamex.StreamEx
import org.apache.commons.lang.StringUtils

@Canonical
abstract class PojoFieldFinder {

    ResolvedMethodDeclaration method

    static PojoFieldFinder fromSetter(ResolvedMethodDeclaration setter) {
        new PojoFieldSetterFinder(setter)
    }

    static PojoFieldFinder fromGetter(ResolvedMethodDeclaration getter) {
        new PojoFieldGetterFinder(getter)
    }

    @InheritConstructors
    static class PojoFieldSetterFinder extends PojoFieldFinder {
        @Override
        Optional<ResolvedFieldDeclaration> tryToFindField() {
            try {
                if (Types.isBooleanType(method.getReturnType())) {
                    String expectedName = 'is' + StringUtils.substringAfter(method.name, 'set').uncapitalize()
                    Optional<ResolvedFieldDeclaration> maybeField = findFieldWithName(expectedName)
                    if (maybeField.present) {
                        return maybeField
                    }
                }
                String expectedName = StringUtils.substringAfter(method.name, 'set').uncapitalize()
                return super.findFieldWithName(expectedName)
            } catch (Exception e) {
                return Optional.empty()
            }
        }
    }

    @InheritConstructors
    static class PojoFieldGetterFinder extends PojoFieldFinder {
        @Override
        Optional<ResolvedFieldDeclaration> tryToFindField() {
            try {
                if (Types.isBooleanType(method.getReturnType())) {
                    if (method.name.startsWith('is')) {
                        String expectedName = StringUtils.substringAfter(method.name, 'is')
                        Optional<ResolvedFieldDeclaration> maybeField = findFieldWithName(expectedName)
                        if (maybeField.present) {
                            return maybeField
                        }
                    }
                }
                String expectedName = StringUtils.substringAfter(method.name, 'get')
                return super.findFieldWithName(expectedName)
            } catch (Exception e) {
                return Optional.empty()
            }
        }
    }

    abstract Optional<ResolvedFieldDeclaration> tryToFindField()

    private Optional<ResolvedFieldDeclaration> findFieldWithName(String name) {
        ResolvedTypeDeclaration rtd = method.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            return StreamEx.of(rtd.asClass().allFields).findFirst {
                it.name == name
            }
        } else if (rtd.enum) {
            //TODO: Add enum support
        }
        return Optional.empty()
    }
}
