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

    static boolean isTypeVisible(TypeDeclaration td) {
        if (td.private) {
            return false
        }
        ASTNodeUtils.parents(td, TypeDeclaration).noneMatch {
            return it.private ||
                    it.isAnonymous() ||
                    isLocalClass(it)
        }
    }

    static boolean isInstantiable(TypeDeclaration td) {
        if (!isTypeVisible(td)) {
            return false
        }

        boolean hasInstantiableConstructor = hasInstantiableConstructor(td)
        boolean allParentsAreVisible = ASTNodeUtils.parents(td, TypeDeclaration).allMatch {
            isInstantiable(it)
        }

        hasInstantiableConstructor && allParentsAreVisible
    }

    static boolean hasInstantiableConstructor(TypeDeclaration td) {
        td.classOrInterfaceDeclaration && (td.asClassOrInterfaceDeclaration().constructors.any { !it.private } ||
                !td.asClassOrInterfaceDeclaration().constructors)
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
