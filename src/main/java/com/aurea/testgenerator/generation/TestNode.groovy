package com.aurea.testgenerator.generation

import com.github.javaparser.ast.Node


abstract class TestNode<T extends Node> implements Dependable {
    TestDependency dependency = new TestDependency()
    T node
}
