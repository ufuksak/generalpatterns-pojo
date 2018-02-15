package com.aurea.testgenerator.generation.merge

import com.aurea.testgenerator.generation.Dependable
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestDependency

class TestNodeMerger {

    static <T extends DependableNode> T appendDependencies(T to, Dependable from) {
        TestDependency fromDependency = from.dependency
        to.dependency.imports.addAll(fromDependency.imports)
        to.dependency.methodSetups.addAll(fromDependency.methodSetups)
        to.dependency.classSetups.addAll(fromDependency.classSetups)
        to.dependency.assignFields.addAll(fromDependency.assignFields)
        to.dependency.classAnnotations.addAll(fromDependency.classAnnotations)

        to
    }

    static <T extends DependableNode> T appendDependencies(T to, Collection<Dependable> froms) {
        froms.each {
            appendDependencies(to, it)
        }
        to
    }

    static TestDependency merge(TestDependency left, TestDependency right) {
        new TestDependency(
                classAnnotations:   left.classAnnotations + right.classAnnotations,
                imports:            left.imports + right.imports,
                fields:             left.fields + right.fields,
                methodSetups:       left.methodSetups + right.methodSetups,
                classSetups:        left.classSetups + right.classSetups,
                assignFields:       left.assignFields + right.assignFields)
    }

    static TestDependency merge(List<TestDependency> dependencies) {
        new TestDependency(
                classAnnotations:   dependencies.classAnnotations.flatten(),
                imports:            dependencies.imports.flatten(),
                fields:             dependencies.fields.flatten(),
                methodSetups:       dependencies.methodSetups.flatten(),
                classSetups:        dependencies.classSetups.flatten(),
                assignFields:       dependencies.assignFields.flatten())
    }
}
