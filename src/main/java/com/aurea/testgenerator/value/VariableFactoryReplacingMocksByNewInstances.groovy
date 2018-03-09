package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.ast.DependableNode
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.type.Type
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class VariableFactoryReplacingMocksByNewInstances {

    ValueFactory valueFactory

    VariableFactoryReplacingMocksByNewInstances(ValueFactory valueFactory) {
        this.valueFactory = valueFactory
    }

    DependableNode<VariableDeclarationExpr> getVariable(String name, Type type) {
        DependableNode<VariableDeclarationExpr> variable = valueFactory.getVariable(name, type).orElseGet {
            NewExpressionBuilder.getNewVariable(name, type)
        }
        replaceMockByNewInstance(variable.node)
        return variable
    }

    List<DependableNode<VariableDeclarationExpr>> getVariableDeclarations(MethodDeclaration method) {
        List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(method.parameters).map { p ->
            getVariable(p.nameAsString,p.type)
        }.toList()
        variables
    }

    private void replaceMockByNewInstance(VariableDeclarationExpr variableDeclarationExpr) {
        List<MethodCallExpr> mockMockMethodCalls = variableDeclarationExpr.findAll (MethodCallExpr).findAll {
            it.name.asString() == "mock"
        }
        mockMockMethodCalls.each {
            String className = it.arguments.first().asClassExpr().type.asString()
            replaceNodeRecursively(variableDeclarationExpr, it, NewExpressionBuilder.buildExpression(className))
        }
    }

    private boolean   replaceNodeRecursively(Node parent, Node node, Node replacement){
        if(parent.replace(node,replacement)){
            return true
        }
        return parent.childNodes.any {replaceNodeRecursively(it,node,replacement)}
    }
}
