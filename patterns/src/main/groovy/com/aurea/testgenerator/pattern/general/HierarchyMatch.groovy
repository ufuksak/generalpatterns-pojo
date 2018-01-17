package com.aurea.testgenerator.pattern.general

import com.aurea.testgenerator.pattern.ClassDescription
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.PatternType
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.NodeList

import java.nio.file.Paths


class HierarchyMatch implements PatternMatch {

    private final static NodeList<ImportDeclaration> EMPTY_NODE_LIST = NodeList.nodeList([])

    private final String declaration
    private final String parent
    private final ClassDescription description

    HierarchyMatch(String declaration, String parent) {
        this.declaration = declaration
        this.parent = parent
        description = new ClassDescription(declaration, "no package", EMPTY_NODE_LIST, Paths.get(""))
    }

    String getDeclaration() {
        return declaration
    }

    String getParent() {
        return parent
    }

    @Override
    ClassDescription description() {
        return description
    }

    @Override
    PatternType type() {
        return null
    }
}
