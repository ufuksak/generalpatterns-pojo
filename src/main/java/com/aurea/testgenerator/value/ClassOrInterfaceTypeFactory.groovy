package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.github.javaparser.ast.type.ClassOrInterfaceType

interface ClassOrInterfaceTypeFactory {

    Optional<TestNodeExpression> get(ClassOrInterfaceType type)

    void setValueFactory(ValueFactory valueFactory)

}