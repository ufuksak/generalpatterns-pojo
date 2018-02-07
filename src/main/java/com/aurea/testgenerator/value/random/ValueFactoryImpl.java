package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.generation.TestNodeExpression;
import com.aurea.testgenerator.generation.TestNodeVariable;
import com.aurea.testgenerator.value.ClassOrInterfaceTypeFactory;
import com.aurea.testgenerator.value.PrimitiveValueFactory;
import com.aurea.testgenerator.value.Types;
import com.aurea.testgenerator.value.ValueFactory;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ValueFactoryImpl implements ValueFactory {

    private final ClassOrInterfaceTypeFactory typesFactory;
    private final PrimitiveValueFactory primitiveFactory;

    @Autowired
    public ValueFactoryImpl(ClassOrInterfaceTypeFactory typesFactory,
                            PrimitiveValueFactory primitiveFactory) {
        typesFactory.setValueFactory(this);
        this.typesFactory = typesFactory;
        this.primitiveFactory = primitiveFactory;
    }

    public Optional<TestNodeExpression> getExpression(Type type) {
        if (type.isPrimitiveType()) {
            return Optional.of(primitiveFactory.get(type.asPrimitiveType()));
        } else if (type.isArrayType()) {
            return getExpression(type.asArrayType().getComponentType())
                    .map(testValue -> {
                        Expression node = testValue.getExpr();
                        ArrayCreationExpr arrayCreationExpr = new ArrayCreationExpr(type.asArrayType().getElementType());
                        arrayCreationExpr.setInitializer(new ArrayInitializerExpr(NodeList.nodeList(node)));
                        testValue.setExpr(arrayCreationExpr);
                        return testValue;
                    });
        } else if (Types.isString(type)) {
            TestNodeExpression testNodeExpression = new TestNodeExpression();
            testNodeExpression.setExpr(typesFactory.get(type.asClassOrInterfaceType()).get().getExpr());
            return Optional.of(testNodeExpression);
        } else if (type.isClassOrInterfaceType()) {
            return typesFactory.get(type.asClassOrInterfaceType());
        }
        return Optional.empty();
    }

    public Optional<TestNodeVariable> getVariable(String name, Type type) {
        Optional<TestNodeExpression> maybeNodeExpression = getExpression(type);
        return maybeNodeExpression.map(nodeExpression -> {
            Expression initializer = nodeExpression.getExpr();
            VariableDeclarator variableDeclarator = new VariableDeclarator(type, name, initializer);
            TestNodeVariable testNodeVariable = new TestNodeVariable();
            testNodeVariable.setDependency(nodeExpression.getDependency());
            testNodeVariable.setExpr(new VariableDeclarationExpr(variableDeclarator));
            return Optional.of(testNodeVariable);
        }).orElse(Optional.empty());
    }
}
