package com.aurea.testgenerator.generation.ast

import com.github.javaparser.ast.Node

class DependableNode<T extends Node> implements Dependable {
    TestDependency dependency = new TestDependency()
    T node

    static <T extends Node> DependableNode<T> from(T node) {
        new DependableNode<>(node: node)
    }

    static <T extends Node> DependableNode<T> from(T node, TestDependency dependency) {
        new DependableNode<>(node: node, dependency: dependency)
    }

    @Override
    String toString() {
        node
    }
}
