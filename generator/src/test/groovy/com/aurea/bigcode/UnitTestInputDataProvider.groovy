package com.aurea.bigcode

import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.MethodOutput
import com.aurea.bigcode.source.SingleUnitTestBuilder
import com.aurea.testgenerator.source.JavaClass
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.Type
import org.junit.rules.TemporaryFolder

class UnitTestInputDataProvider {

    static final METHOD_UNDER_TEST_SOURCE = '''
            package com.aurea.sample;
            
            public class Sample {
            
                public int addNumbers(int a, int b) {
                    return a + b;
                }
            }'''

    static final METHOD_UNDER_TEST_SOURCE_FLOATING_POINT = '''
            package com.aurea.sample;
            
            public class Sample {
            
                public float addNumbers(float a, int b) {
                    return a + b;
                }
            }'''

    static final CLASS_UNDER_TEST = new JavaClass('com.aurea.sample.Sample')

    static UnitTest createSingleTestCaseIntUnitTest(TemporaryFolder folder) {

        def methodUnderTest = methodDeclarationFromSource(folder, METHOD_UNDER_TEST_SOURCE)

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

    static UnitTest createSingleTestCaseFloatUnitTest(TemporaryFolder folder) {

        def methodUnderTest = methodDeclarationFromSource(folder, METHOD_UNDER_TEST_SOURCE_FLOATING_POINT)

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

    static UnitTest createMultilpeTestCasesIntUnitTest(TemporaryFolder folder) {
        def methodUnderTest = methodDeclarationFromSource(folder, METHOD_UNDER_TEST_SOURCE)

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

    private static MethodDeclaration methodDeclarationFromSource(TemporaryFolder folder, String code) {
        File file = folder.newFile("ClassUnderTest.java")
        file.text = code
        CompilationUnit cu = JavaParser.parse(file)
        file.delete()
        cu.findFirst(MethodDeclaration).get()
    }
}
