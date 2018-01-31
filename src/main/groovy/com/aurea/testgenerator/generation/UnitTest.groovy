package com.aurea.testgenerator.generation

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import groovy.transform.Canonical

@Canonical
class UnitTest {
    Set<AnnotationExpr> classAnnotations = []
    Set<ImportDeclaration> imports = []
    Set<FieldDeclaration> fields = []
    Optional<BlockStmt> methodSetup = Optional.empty()
    Optional<BlockStmt> classSetup = Optional.empty()
    MethodDeclaration method
}
