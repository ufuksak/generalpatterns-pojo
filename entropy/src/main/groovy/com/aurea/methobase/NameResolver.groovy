package com.aurea.methobase

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt

class NameResolver {

    TypeDeclaration td

    NameResolver(TypeDeclaration td) {
        this.td = td
    }

    String getName(Unit unit) {
        CompilationUnit cu = unit.cu
        String packageName = cu.findFirst(PackageDeclaration)
                               .map { it.nameAsString + '.'}
                               .orElse("")
        packageName + getFullNameOfType()
    }

    String getFullNameOfType() {
        List<String> names = []
        getNameRecursively(td, names)
    }

    String getNameRecursively(TypeDeclaration td, List<String> names) {
        Optional<String> classBlockIndex = getLocalClassDeclarationIndex(td)
        names << classBlockIndex.orElse("") + td.nameAsString
        td.getAncestorOfType(TypeDeclaration).ifPresent { TypeDeclaration parent ->
            getNameRecursively(parent, names)
        }
        names.reverse().join('$')
    }

    static Optional<String> getLocalClassDeclarationIndex(TypeDeclaration td) {
        Optional<LocalClassDeclarationStmt> lcds = getLocalClassDeclarationStmt(td)
        lcds.map {
            //TODO: "1" is okay answer assuming that there are no input
            //TODO: with two same name local classes in different methods of
            //TODO: single class. Chances of this happening are slim.
            "1"
        }
    }

    static Optional<LocalClassDeclarationStmt> getLocalClassDeclarationStmt(TypeDeclaration td) {
        Optional<Node> maybeParent = td.parentNode
        while (maybeParent.present) {
            Node parent = maybeParent.get()
            if (parent instanceof LocalClassDeclarationStmt) {
                return Optional.of(parent)
            } else if (TypeDeclaration.isAssignableFrom(parent.class)) {
                return Optional.empty()
            }
            maybeParent = parent.parentNode
        }
        Optional.empty()
    }
}
