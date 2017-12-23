package com.aurea.methobase.meta

import com.github.javaparser.ast.Modifier
import groovy.transform.Canonical

@Canonical
class ClassMetaInformation extends MetaInformation {
    String name
    String extendedType, outerType
    List<Modifier> modifiers
    Boolean isStatic
    Boolean isAbstract
    Boolean hasStaticBlocks
    Boolean hasConstructors
}
