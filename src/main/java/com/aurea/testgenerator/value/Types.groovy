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

    static final Set<String> KNOWN_ITERABLE_TYPES = [
            'Iterable',
            'java.util.Iterable',
    ]

    static final Set<String> KNOWN_COMPARABLE_TYPES = [
            'BigDecimal',
            'java.math.BigDecimal']

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
        type.nameAsString in KNOWN_COLLECTION_TYPES
    }

    static boolean isCollection(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_COLLECTION_TYPES
    }

    static boolean isCollection(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_COLLECTION_TYPES)
    }

    static boolean isIterable(ClassOrInterfaceType type) {
        type.nameAsString in KNOWN_ITERABLE_TYPES
    }

    static boolean isIterable(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_ITERABLE_TYPES
    }

    static boolean isIterable(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_ITERABLE_TYPES)
    }

    static boolean isList(ClassOrInterfaceType type) {
        type.nameAsString in KNOWN_LIST_TYPES
    }

    static boolean isList(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_LIST_TYPES
    }

    static boolean isList(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_LIST_TYPES)
    }

    static boolean isSet(ClassOrInterfaceType type) {
        type.nameAsString in KNOWN_SET_TYPES
    }

    static boolean isSet(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_SET_TYPES
    }

    static boolean isSet(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_SET_TYPES)
    }

    static boolean isComparable(ClassOrInterfaceType type) {
        type.nameAsString in KNOWN_COMPARABLE_TYPES
    }

    static boolean isComparable(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_COMPARABLE_TYPES
    }

    static boolean isComparable(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_COMPARABLE_TYPES)
    }

    static boolean isMap(ClassOrInterfaceType type) {
        type.nameAsString in KNOWN_MAP_TYPES
    }

    static boolean isMap(ResolvedReferenceType type) {
        type.qualifiedName in KNOWN_MAP_TYPES
    }

    static boolean isMap(ResolvedType type) {
        type.referenceType && (type.asReferenceType().qualifiedName in KNOWN_MAP_TYPES)
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
