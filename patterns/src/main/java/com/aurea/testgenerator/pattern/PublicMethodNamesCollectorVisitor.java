package com.aurea.testgenerator.pattern;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class PublicMethodNamesCollectorVisitor extends VoidVisitorAdapter<Object> {

    private final List<String> methodNames = new ArrayList<>();

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        if (n.isPublic()) {
            methodNames.add(n.getNameAsString());
        }
        super.visit(n, arg);
    }

    public List<String> getMethodNames() {
        return methodNames;
    }
}
