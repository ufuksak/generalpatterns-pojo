package com.aurea.methobase.meta

import com.github.javaparser.ast.Modifier
import groovy.transform.Canonical

@Canonical
class MethodMetaInformation extends MetaInformation {
    String name
    String returnType
    List<Modifier> modifiers
    List<String> genericParameters
    List<String> thrownExceptions
    List<String> parameters
    Set<String> referencedTypes
    Boolean isStatic
    Boolean isAbstract
    Boolean isPure
    Integer cognitiveComplexity
}
