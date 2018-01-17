package com.aurea.testgenerator.pattern.easy

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.*

import static com.aurea.testgenerator.ast.ASTNodeUtils.*
import static java.util.Optional.empty
import static java.util.Optional.of

class LiteralResolver {

    static final String NULL = 'null'

    Optional<String> tryFindLiteralExpression(Node node) {
        if (!hasOnlyChildsSubTypesOf(node, BinaryExpr.class, LiteralExpr.class)) {
            return empty()
        }
        if (hasAtLeastOneStringLiteral(node)) {
            return of(wrap(concatenate(node)))
        } else if (hasOnlyChildsSubTypesOf(node, NullLiteralExpr.class) && !Expression.class.isAssignableFrom(node.class)) {
            return of(NULL)
        } else {
            return findChildSubTypeOf(Expression.class, node).map { it.toString() }
        }
    }

    private boolean hasAtLeastOneStringLiteral(Node node) {
        !findChildsSubTypesOf(StringLiteralExpr.class, node).isEmpty()
    }

    private String wrap(String text) {
        '"' + text + '"'
    }

    private String concatenate(Node node) {
        findChildsSubTypesOf(LiteralStringValueExpr.class, node).collect { it.value }.join("")
    }
}
