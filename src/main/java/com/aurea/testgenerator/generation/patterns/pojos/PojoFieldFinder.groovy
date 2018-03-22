package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.value.Types
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.jasongoodwin.monads.Try
import groovy.transform.Canonical
import one.util.streamex.StreamEx
import org.apache.commons.lang.StringUtils

@Canonical
abstract class PojoFieldFinder {

    static Optional<ResolvedFieldDeclaration> findSetterField(ResolvedMethodDeclaration method) {
        Try.ofFailable {
            if (method.numberOfParams == 1 && Types.isBooleanType(method.getParam(0).type) && validPrefix(method.name, 'set')) {
                def expectedName = method.name.replaceFirst('set', 'is')
                Optional<ResolvedFieldDeclaration> maybeField = findFieldWithName(method, expectedName)
                if (maybeField.present) {
                    return maybeField
                }
            }
            def expectedName = removePrefix(method.name, 'set')
            findFieldWithName(method, expectedName)
        }.orElse(Optional.empty())
    }

    static Optional<ResolvedFieldDeclaration> findGetterField(ResolvedMethodDeclaration method) {
        Try.ofFailable {
            if (Types.isBooleanType(method.getReturnType())) {
                if (validPrefix(method.name, 'is')) {
                    String expectedName = removePrefix(method.name, 'is')
                    Optional<ResolvedFieldDeclaration> maybeField = findFieldWithName(method, expectedName)
                    if (maybeField.present) {
                        return maybeField
                    }
                }

                if (validPrefix(method.name, 'get')) {
                    String expectedName = method.name.replaceFirst('get', 'is')
                    Optional<ResolvedFieldDeclaration> maybeField = findFieldWithName(method, expectedName)
                    if (maybeField.present) {
                        return maybeField
                    }
                }
            }
            String expectedName = removePrefix(method.name, 'get')
            findFieldWithName(method, expectedName)
        }.orElse(Optional.empty())
    }

    private static String removePrefix(String name, String prefix) {
        StringUtils.removeStart(name, prefix).uncapitalize()
    }

    private static Optional<ResolvedFieldDeclaration> findFieldWithName(ResolvedMethodDeclaration method, String name) {
        ResolvedTypeDeclaration rtd = method.declaringType()
        if (rtd.class || rtd.anonymousClass) {
            def fields = rtd.asClass().allFields
            return StreamEx.of(fields).findFirst { it.name == name }
        }

        if (rtd.enum) {
            //TODO: Add enum support
        }

        Optional.empty()
    }

    static boolean validPrefix(String name, String prefix) {
        name.size() > prefix.size() && name.startsWith(prefix) && StringUtils.isAllUpperCase(name[prefix.size()])
    }
}
