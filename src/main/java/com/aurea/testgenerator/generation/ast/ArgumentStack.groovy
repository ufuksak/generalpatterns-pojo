package com.aurea.testgenerator.generation.ast

import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.nodeTypes.NodeWithArguments
import com.github.javaparser.ast.nodeTypes.NodeWithParameters
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt

class ArgumentStack {

    Map<String, Expression> stack = new HashMap<>()

    void goDeeper(NodeWithParameters called, ExplicitConstructorInvocationStmt caller) {
        goDeeper(called, caller.arguments)
    }

    void goDeeper(NodeWithParameters called, NodeWithArguments caller) {
        goDeeper(called, caller.arguments)
    }

    void goDeeper(NodeWithParameters called, Collection<Expression> arguments) {
        updateStack(called.parameters, arguments)
    }

    void updateStack(Collection<Parameter> parameters, Collection<Expression> arguments) {
        assert parameters.size() == arguments.size()

        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters[i]
            Expression argument = arguments[i]
            if (argument.nameExpr) {
                String reference = argument.asNameExpr()
                argument = stack.getOrDefault(reference, argument)
            }
            stack.putIfAbsent(parameter.nameAsString, argument)
        }
    }
}
