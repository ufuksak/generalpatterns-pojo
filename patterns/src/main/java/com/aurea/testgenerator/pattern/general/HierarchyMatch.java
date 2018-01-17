package com.aurea.testgenerator.pattern.general;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.pattern.PatternType;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import java.nio.file.Paths;
import java.util.Collections;

public class HierarchyMatch implements PatternMatch {

    private final static NodeList<ImportDeclaration> EMPTY_NODE_LIST = NodeList.nodeList(Collections.emptyList());

    private final String declaration;
    private final String parent;
    private final ClassDescription description;

    public HierarchyMatch(String declaration, String parent) {
        this.declaration = declaration;
        this.parent = parent;
        description = new ClassDescription(declaration, "no package", EMPTY_NODE_LIST, Paths.get(""));
    }

    public String getDeclaration() {
        return declaration;
    }

    public String getParent() {
        return parent;
    }

    @Override
    public ClassDescription description() {
        return description;
    }

    @Override
    public PatternType type() {
        return null;
    }
}
