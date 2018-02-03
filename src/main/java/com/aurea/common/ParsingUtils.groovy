package com.aurea.common

import org.apache.commons.lang3.StringUtils

import java.nio.file.Path

class ParsingUtils {
    static String parseSimpleName(String name) {
        isFullName(name) ? StringUtils.substringAfterLast(name, '.') : name
    }

    static String createFullName(String packageName, String className) {
        packageName + '.' + className
    }

    static boolean isFullName(String name) {
        name.contains('.')
    }

    static String parsePackage(String name) {
        StringUtils.substringBeforeLast(name, '.')
    }

    static String parseJavaClassName(Path path) {
        StringUtils.removeEndIgnoreCase(path.fileName.toString(), '.java')
    }
}
