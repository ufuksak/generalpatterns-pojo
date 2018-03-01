package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.generation.ast.DependableNode;
import com.aurea.testgenerator.value.MockExpressionBuilder;
import com.aurea.testgenerator.value.PrimitiveValueFactory;
import com.aurea.testgenerator.value.ReferenceTypeFactory;
import com.aurea.testgenerator.value.Resolution;
import com.aurea.testgenerator.value.ValueFactory;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ValueFactoryImpl implements ValueFactory {

    private final ReferenceTypeFactory typesFactory;
    private final PrimitiveValueFactory primitiveFactory;

    @Autowired
    public ValueFactoryImpl(ReferenceTypeFactory typesFactory,
                            PrimitiveValueFactory primitiveFactory) {
        typesFactory.setValueFactory(this);
        this.typesFactory = typesFactory;
        this.primitiveFactory = primitiveFactory;
    }

    @Override
    public Optional<DependableNode<Expression>> getExpression(ResolvedType type) {
        if (type.isPrimitive()) {
            return Optional.of(primitiveFactory.get(type.asPrimitive()));
        }
        if (type.isArray()) {
            return getExpression(type.asArrayType().getComponentType())
                    .map(testValue -> {
                        Expression node = testValue.getNode();
                        ResolvedType componentType = type.asArrayType().getComponentType();
                        ArrayCreationExpr arrayCreationExpr;
                        if (componentType.isPrimitive()) {
                            arrayCreationExpr = new ArrayCreationExpr(
                                    JavaParser.parseType(componentType.asPrimitive().describe()),
                                    NodeList.nodeList(new ArrayCreationLevel()),
                                    new ArrayInitializerExpr(NodeList.nodeList(node)));

                        } else if (componentType.isReferenceType()) {
                            arrayCreationExpr = new ArrayCreationExpr(
                                    JavaParser.parseClassOrInterfaceType(componentType.asReferenceType().getQualifiedName()),
                                    NodeList.nodeList(new ArrayCreationLevel()),
                                    new ArrayInitializerExpr(NodeList.nodeList(node)));
                        } else {
                            throw new UnsupportedOperationException("Unknown component type of the array: " + componentType);
                        }
                        testValue.setNode(arrayCreationExpr);
                        return testValue;
                    });
        }
        if (type.isReferenceType()) {
            return typesFactory.get(type.asReferenceType());
        }
        return Optional.empty();
    }

    @Override
    public DependableNode<Expression> getStubExpression(Type type) {
        return MockExpressionBuilder.build(type.toString());
    }

    @Override
    public Optional<DependableNode<VariableDeclarationExpr>> getVariable(String name, Type type) {
        Optional<ResolvedType> resolvedType = Resolution.tryResolve(type);
        return resolvedType.flatMap(this::getExpression).flatMap(nodeExpression -> {
            Expression initializer = nodeExpression.getNode();
            VariableDeclarator variableDeclarator = new VariableDeclarator(type.clone(), name, initializer);
            DependableNode<VariableDeclarationExpr> dependableNode = DependableNode.from(
                    new VariableDeclarationExpr(variableDeclarator),
                    nodeExpression.getDependency());
            return Optional.of(dependableNode);
        });
    }
}
