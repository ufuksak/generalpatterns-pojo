package com.aurea.testgenerator.generation


class TestDependencyMerger {

    static TestDependency merge(TestDependency left, TestDependency right) {
        TestDependency result = new TestDependency()
        result.imports.addAll(left.imports)
        result.imports.addAll(right.imports)

        result.methodSetups.addAll(left.methodSetups)
        result.methodSetups.addAll(right.methodSetups)

        result.classSetups.addAll(left.classSetups)
        result.classSetups.addAll(right.classSetups)

        result.fields.addAll(left.fields)
        result.fields.addAll(right.fields)

        result.classAnnotations.addAll(left.classAnnotations)
        result.classAnnotations.addAll(right.classAnnotations)

        result.assignFields.addAll(left.assignFields)
        result.assignFields.addAll(right.assignFields)

        result
    }

    static TestDependency merge(List<TestDependency> dependencies) {
        TestDependency result = new TestDependency()
        dependencies.each {
            result.imports.addAll it.imports
            result.methodSetups.addAll it.methodSetups
            result.classSetups.addAll it.classSetups
            result.fields.addAll it.fields
            result.classAnnotations.addAll it.classAnnotations
            result.assignFields.addAll it.assignFields
        }
        result
    }
}
