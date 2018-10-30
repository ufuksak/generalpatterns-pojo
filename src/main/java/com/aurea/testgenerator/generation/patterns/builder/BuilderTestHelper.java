package com.aurea.testgenerator.generation.patterns.builder;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Strings;
import java.util.Optional;
import one.util.streamex.StreamEx;

class BuilderTestHelper {

    private static final String BUILDER_SUFFIX = "Builder";
    static final String BUILD_METHOD = "build";
    private static final String GET_PREFIX = "get";

    static boolean isBuilder(ClassOrInterfaceDeclaration classDeclaration) {
        if (!classDeclaration.getNameAsString().endsWith(BUILDER_SUFFIX)) {
            return false;
        }

        return findBuilderMethod(classDeclaration).isPresent();
    }

    static String buildGetterName(MethodDeclaration method) {
        return GET_PREFIX + firstToUpperCase(method.getNameAsString());
    }

    static String firstToUpperCase(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }

        if (text.length() == 1) {
            return text.toUpperCase();
        }

        return text.substring(0, 1).toUpperCase()
                + text.substring(1);
    }

    static Optional<MethodDeclaration> findBuilderMethod(ClassOrInterfaceDeclaration classDeclaration) {
        return StreamEx.of(classDeclaration.getMethods())
                .findFirst(it -> it.getNameAsString().equals(BUILD_METHOD) && it.getParameters().isEmpty());
    }
}
