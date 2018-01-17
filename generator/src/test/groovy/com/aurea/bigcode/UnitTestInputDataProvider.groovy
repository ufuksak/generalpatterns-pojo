package com.aurea.bigcode

import UnitHelper
import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.MethodOutput
import com.aurea.bigcode.source.SingleUnitTestBuilder
import com.aurea.testgenerator.source.JavaClass
import com.github.javaparser.ast.type.Type

class UnitTestInputDataProvider {

    static final METHOD_UNDER_TEST_SOURCE = '''
                public int addNumbers(int a, int b) {
                    return a + b;
                }'''

    static final METHOD_UNDER_TEST_SOURCE_FLOATING_POINT = '''
                public float addNumbers(float a, int b) {
                    return a + b;
                }'''

    static final CLASS_UNDER_TEST = new JavaClass('com.aurea.sample.Sample')

    static UnitTest createSingleTestCaseIntUnitTest() {

        def methodUnderTest = UnitHelper.getMethodFromSource(METHOD_UNDER_TEST_SOURCE)

        Type intType = methodUnderTest.parameters.first().type
        MethodInput methodInput = MethodInput.ofValues(
                new Value(intType, 3, '3'),
                new Value(intType, 2, '2'))

        MethodOutput methodOutput = new MethodOutput(intType, '5')
        TestCase testCase = new TestCase(methodInput, methodOutput)

        SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest)
                .withTestCase(testCase)
                .withSuffix('int')
                .build()
    }

    static UnitTest createSingleTestCaseFloatUnitTest() {

        def methodUnderTest = UnitHelper.getMethodFromSource(METHOD_UNDER_TEST_SOURCE_FLOATING_POINT)

        Type floatType = methodUnderTest.parameters[0].type
        Type intType = methodUnderTest.parameters[1].type
        MethodInput methodInput = MethodInput.ofValues(
                new Value(floatType, 3, '3.1F'),
                new Value(intType, 2, '2'))

        MethodOutput methodOutput = new MethodOutput(floatType, '5.1F')
        TestCase testCase = new TestCase(methodInput, methodOutput)

        SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest)
                .withTestCase(testCase)
                .withSuffix('float')
                .build()
    }

    static UnitTest createMultilpeTestCasesIntUnitTest() {
        def methodUnderTest = UnitHelper.getMethodFromSource(METHOD_UNDER_TEST_SOURCE)

        def intType = methodUnderTest.parameters.first().type
        List<TestCase> testCases = [
                new TestCase(
                        MethodInput.ofValues(new Value(intType, 3, '3'), new Value(intType, 2, '2')),
                        new MethodOutput(intType, '5')),

                new TestCase(
                        MethodInput.ofValues(new Value(intType, 0, '0'), new Value(intType, 7, '7')),
                        new MethodOutput(intType, '7'))]

        SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest)
                .withTestCases(testCases)
                .withSuffix('mult')
                .build()
    }
}
