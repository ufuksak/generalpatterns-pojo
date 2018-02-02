package com.aurea.testgenerator.generation

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import groovy.transform.Canonical


interface TestNode<T extends Node> {

    Dependency getDependency()
    Optional<T> getNode()


//    Optional<T> node
//
//    TestNode<T> addSetups(TestNode<? extends Node> n) {
//        classAnnotations.addAll(n.classAnnotations)
//        imports.addAll(n.imports)
//        fields.addAll(n.fields)
//        beforeMethods.addAll(n.beforeMethods)
//        beforeClassMethods.addAll(n.beforeClassMethods)
//        this
//    }
}
