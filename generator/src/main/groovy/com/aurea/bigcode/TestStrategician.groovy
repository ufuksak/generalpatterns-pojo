package com.aurea.bigcode


class TestStrategician {
    TestGenerator pickGenerator(TestedMethod testedMethod) {
        if (isPure(testedMethod) && useOnlyPrimitives(testedMethod)) {
            return TestGenerators.forParameterizedTests()
        }
        return TestGenerators.forParameterizedTests()
    }

    static boolean isPure(TestedMethod testedMethod) {
        true
    }

    static boolean useOnlyPrimitives(TestedMethod testedMethod) {
        true
    }
}
