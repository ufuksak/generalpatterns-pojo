package com.aurea.testgenerator.generation

import com.github.javaparser.ast.Node

interface TestNode<T extends Node> {

    TestDependency getDependency()

    Optional<T> getNode()
}
