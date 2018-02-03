package com.aurea.testgenerator.value

import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type


class Types {

    static final OBJECT = new ClassOrInterfaceType("Object")

    static boolean isString(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType coit = type.asClassOrInterfaceType()
            if (coit.nameAsString == 'String' || coit.nameAsString == 'java.lang.String') {
                return true
            }
        }
        return false
    }

    static boolean isList(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'List' || name == 'java.util.List' || name == 'ArrayList' || name == 'java.util.ArrayList'
    }

    static boolean isSet(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Set' || name == 'java.util.Set' || name == 'HashSet' || name == 'java.util.HashSet'
    }

    static boolean isCollection(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Collection' || name == 'java.util.Collection'
    }

    static boolean isIterable(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Iterable' || name == 'java.util.Iterable'
    }

    static boolean isMap(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Map' || name == 'java.util.Map' || name == 'HashMap' || name == 'java.util.HashMap'
    }

    static boolean isDate(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Date' || name == 'java.util.Date' || name == 'java.sql.Date'
    }
    static boolean isSqlDate(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'java.sql.Date'
    }
}
