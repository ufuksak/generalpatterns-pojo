package com.aurea.testgenerator.generation

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import groovy.transform.Canonical


@Canonical
class TestNode<T extends Node> {
    Set<AnnotationExpr> classAnnotations = []
    Set<ImportDeclaration> imports = []
    Set<FieldDeclaration> fields = []
    Optional<MethodDeclaration> methodSetup = Optional.empty()
    Optional<MethodDeclaration> classSetup = Optional.empty()
    T node
}
