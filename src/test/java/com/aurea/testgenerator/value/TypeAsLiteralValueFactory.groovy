package com.aurea.testgenerator.value

import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestNodeVariable
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.type.Type

class TypeAsLiteralValueFactory implements ValueFactory {
    @Override
    Optional<TestNodeExpression> getExpression(Type type) {
        Optional.of(new TestNodeExpression(expr: new ClassExpr(type)))
    }

    @Override
    Optional<TestNodeVariable> getVariable(Type type) {
        Optional.of(new TestNodeVariable(node: Optional.of(new VariableDeclarationExpr(type, "foo"))))
    }
}
