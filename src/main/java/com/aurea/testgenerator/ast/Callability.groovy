package com.aurea.testgenerator.ast

import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration

class Callability {

    static boolean isCallableFromTests(CallableDeclaration cd) {
        if (cd.private) {
            return false
        }
        ASTNodeUtils.parents(cd, TypeDeclaration).noneMatch {
            return it.private ||
                    it.isAnonymous() ||
                    isAbstract(it) ||
                    isLocalClass(it)
        }
    }

    static boolean isLocalClass(TypeDeclaration td) {
        td instanceof ClassOrInterfaceDeclaration &&
                (td as ClassOrInterfaceDeclaration).isLocalClassDeclaration()
    }

    static boolean isAbstract(TypeDeclaration td) {
        td instanceof ClassOrInterfaceDeclaration &&
                (td as ClassOrInterfaceDeclaration).isAbstract()
    }
}
