package com.aurea.testgenerator.generation.ast

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import groovy.transform.Canonical

@Canonical
class TestDependency {
    Set<AnnotationExpr> classAnnotations = []
    Set<ImportDeclaration> imports = []
    Set<FieldDeclaration> fields = []
    List<MethodDeclaration> methodSetups = []
    List<MethodDeclaration> classSetups = []
    List<VariableDeclarationExpr> assignFields = []
}
