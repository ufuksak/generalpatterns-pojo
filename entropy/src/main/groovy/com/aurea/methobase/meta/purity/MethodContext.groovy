package com.aurea.methobase.meta.purity

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import groovy.transform.Canonical

@Canonical
class MethodContext {

    NodeList<Parameter> methodParameters
    List<VariableDeclarator> classVariables

    static MethodContext buildForMethod(MethodDeclaration md) {
        List<VariableDeclarator> classVariables = []

        findAllFieldNames(md, classVariables)

        new MethodContext(
                methodParameters: md.parameters,
                classVariables: classVariables)
    }

    private static void findAllFieldNames(Node node, List<VariableDeclarator> classVariables) {
        if (node == null) {
            return
        }
        ClassOrInterfaceDeclaration coid =
                ASTNodeUtils.findNextParentSubTypeOf(ClassOrInterfaceDeclaration, node)
        if (coid != null) {
            classVariables.addAll(coid.fields.variables.flatten().toList() as Collection<? extends VariableDeclarator>)
            findAllFieldNames(coid, classVariables)
        }
    }

    boolean isMethodParameter(String name) {
        methodParameters.any {it.nameAsString == name }
    }

    boolean isClassField(String name) {
        classVariables.any {it.nameAsString == name }
    }

    boolean outOfMethodScope(String name) {
        isMethodParameter(name) || isClassField(name)
    }

    Optional<Parameter> getMethodParameterByName(String name) {
        Optional.ofNullable(methodParameters.find {it.nameAsString == name})
    }

    Optional<VariableDeclarator> getClassVariableByName(String name) {
        Optional.ofNullable(classVariables.find { it.nameAsString == name})
    }
}
