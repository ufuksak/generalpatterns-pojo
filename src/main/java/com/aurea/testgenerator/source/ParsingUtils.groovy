package com.aurea.testgenerator.source

import org.apache.commons.lang3.StringUtils

import java.nio.file.Path

class ParsingUtils {
    static String parseSimpleName(String name) {
        isFullName(name) ? StringUtils.substringAfterLast(name, ".") : name
    }

    static boolean isFullName(String name) {
        name.contains(".")
    }

    static String parseJavaClassName(Path path) {
        StringUtils.removeEnd(path.fileName.toString(), '.java')
    }
}
