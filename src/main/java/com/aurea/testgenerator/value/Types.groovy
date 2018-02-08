package com.aurea.testgenerator.value

import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType


class Types {

    static final OBJECT = new ClassOrInterfaceType("Object")

    static final Set<String> KNOWN_COLLECTION_TYPES = [
            'Iterable',
            'java.util.Iterable',
            'Collection',
            'java.util.Collection',
            'List',
            'java.util.List',
            'Set',
            'java.util.Set',
            'SortedSet',
            'java.util.SortedSet',
    ]

    static final Set<String> KNOWN_COMPARABLE_TYPES = [
            'BigDecimal',
            'BigInteger',
            'java.math.BigDecimal',
            'java.math.BigInteger']

    static final Set<String> KNOWN_MAP_TYPES = [
            'Map',
            'java.util.Map',
            'HashMap',
            'java.util.HashMap',
    ]

    static final Set<String> KNOWN_LIST_TYPES = [
            'List',
            'java.util.List',
            'ArrayList',
            'java.util.ArrayList',
    ]

    static final Set<String> KNOWN_SET_TYPES = [
            'Set',
            'java.util.Set',
            'HashSet',
            'java.util.HashSet',
    ]

    static boolean isString(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType coit = type.asClassOrInterfaceType()
            if (coit.nameAsString == 'String' || coit.nameAsString == 'java.lang.String') {
                return true
            }
        }
        return false
    }

    static boolean isDate(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'Date' || name == 'java.util.Date' || name == 'java.sql.Date'
    }

    static boolean isSqlDate(ClassOrInterfaceType type) {
        String name = type.nameAsString
        name == 'java.sql.Date'
    }

    static boolean isCollection(ClassOrInterfaceType type) {
        KNOWN_COLLECTION_TYPES.contains(type.nameAsString)
    }

    static boolean isCollection(ResolvedReferenceType type) {
        KNOWN_COLLECTION_TYPES.contains(type.qualifiedName)
    }

    static boolean isCollection(ResolvedType type) {
        type.referenceType && KNOWN_COLLECTION_TYPES.contains(type.asReferenceType().qualifiedName)
    }

    static boolean isList(ClassOrInterfaceType type) {
        KNOWN_LIST_TYPES.contains(type.nameAsString)
    }

    static boolean isList(ResolvedReferenceType type) {
        KNOWN_LIST_TYPES.contains(type.qualifiedName)
    }

    static boolean isList(ResolvedType type) {
        type.referenceType && KNOWN_LIST_TYPES.contains(type.asReferenceType().qualifiedName)
    }

    static boolean isSet(ClassOrInterfaceType type) {
        KNOWN_SET_TYPES.contains(type.nameAsString)
    }

    static boolean isSet(ResolvedReferenceType type) {
        KNOWN_SET_TYPES.contains(type.qualifiedName)
    }

    static boolean isSet(ResolvedType type) {
        type.referenceType && KNOWN_SET_TYPES.contains(type.asReferenceType().qualifiedName)
    }

    static boolean isComparable(ClassOrInterfaceType type) {
        KNOWN_COMPARABLE_TYPES.contains(type.nameAsString)
    }

    static boolean isComparable(ResolvedReferenceType type) {
        KNOWN_COMPARABLE_TYPES.contains(type.qualifiedName)
    }

    static boolean isComparable(ResolvedType type) {
        type.referenceType && KNOWN_COMPARABLE_TYPES.contains(type.asReferenceType().qualifiedName)
    }

    static boolean isMap(ClassOrInterfaceType type) {
        KNOWN_MAP_TYPES.contains(type.nameAsString)
    }

    static boolean isMap(ResolvedReferenceType type) {
        KNOWN_MAP_TYPES.contains(type.qualifiedName)
    }

    static boolean isMap(ResolvedType type) {
        type.referenceType && KNOWN_MAP_TYPES.contains(type.asReferenceType().qualifiedName)
    }

    static boolean isBoxedPrimitive(ResolvedReferenceType type) {
        for (ResolvedPrimitiveType primitive : ResolvedPrimitiveType.ALL) {
            if (primitive.boxTypeQName == type.qualifiedName) {
                return true
            }
        }
        return false
    }

    static ResolvedPrimitiveType unbox(ResolvedReferenceType type) {
        for (ResolvedPrimitiveType primitive : ResolvedPrimitiveType.ALL) {
            if (primitive.boxTypeQName == type.qualifiedName) {
                return primitive
            }
        }
        return null
    }
}
