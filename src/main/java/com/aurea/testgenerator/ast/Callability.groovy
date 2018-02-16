package com.aurea.testgenerator.ast

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.resolution.types.ResolvedType

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

    static boolean canBeCalledWithArguments(CallableDeclaration callable, NodeList<Expression> arguments) {
        NodeList<Parameter> parameters = callable.parameters
        if (arguments.size() != parameters.size()) {
            return false
        }

        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters[i]
            Expression argument = arguments[i]
            ResolvedType parameterType = parameter.type.resolve()
            ResolvedType argumentType = argument.calculateResolvedType()
            if (parameterType != argumentType) {
                return false
            }
        }
        return true
    }

}
