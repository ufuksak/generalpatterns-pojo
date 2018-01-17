package com.aurea.testgenerator.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Collections;
import java.util.List;

public class ArgsFetchVisitor extends VoidVisitorAdapter<Object> {

    private List<String> args = Collections.emptyList();

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        args = ASTNodeUtils.getMethodArgs(n);
        super.visit(n, arg);
    }

    public List<String> getArgs() {
        return args;
    }
}
