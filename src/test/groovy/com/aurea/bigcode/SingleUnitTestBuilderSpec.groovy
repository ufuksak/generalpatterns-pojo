package com.aurea.bigcode

import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.MethodOutput

import com.aurea.testgenerator.source.JavaClass
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.Type
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SingleUnitTestBuilderSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    static final METHOD_UNDER_TEST_SOURCE = '''
            package com.aurea.sample;
            
            public class Sample {
            
                public static int addNumbers(int a, int b) {
                    return a + b;
                }
            }'''

    static final METHOD_UNDER_TEST_SOURCE_FLOATING_POINT = '''
            package com.aurea.sample;
            
            public class Sample {
            
                public static float addNumbers(float a, int b) {
                    return a + b;
                }
            }'''

    static final CLASS_UNDER_TEST = new JavaClass('com.aurea.sample.Sample')

    def "creating single test case unit test"() {
        setup:

        def methodUnderTest = methodDeclarationFromSource(METHOD_UNDER_TEST_SOURCE)

        Type intType = methodUnderTest.parameters.first().type
        MethodInput methodInput = MethodInput.ofValues(
                new Value(intType, 3, '3'),
                new Value(intType, 2, '2'))

        MethodOutput methodOutput = new MethodOutput(intType, '5')
        TestCase testCase = new TestCase(methodInput, methodOutput)

        when:
        def result = SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest).withTestCase(testCase).build()

        then:
        result.method.getSourceCode() ==
'''
    @Test
    public void test_addNumbers() {
        int result = Sample.addNumbers(3, 2);

        assertThat(result).isEqualTo(5);
    }
'''
    }

    def "creating single test case unit test for floating point type"() {
        setup:
        def methodUnderTest = methodDeclarationFromSource(METHOD_UNDER_TEST_SOURCE_FLOATING_POINT)
        Type floatType = methodUnderTest.parameters[0].type
        Type intType = methodUnderTest.parameters[1].type
        MethodInput methodInput = MethodInput.ofValues(
                new Value(floatType, 3, '3.1F'),
                new Value(intType, 2, '2'))

        MethodOutput methodOutput = new MethodOutput(floatType, '5.1F')
        TestCase testCase = new TestCase(methodInput, methodOutput)

        when:
        def result = SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest).withTestCase(testCase).build()

        then:
        result.method.getSourceCode() ==
'''
    @Test
    public void test_addNumbers() {
        float result = Sample.addNumbers(3.1F, 2);

        assertThat(result).isCloseTo(5.1F, Offset.offset(0.001));
    }
'''
    }

    def "creating parametrized unit test"() {
        setup:
        def methodUnderTest = methodDeclarationFromSource(METHOD_UNDER_TEST_SOURCE)
        def intType = methodUnderTest.parameters.first().type

        List<TestCase> testCases = [
                new TestCase(
                        MethodInput.ofValues(new Value(intType, 3, '3'), new Value(intType, 2, '2')),
                        new MethodOutput(intType, '5')),

                new TestCase(
                        MethodInput.ofValues(new Value(intType, 0, '0'), new Value(intType, 7, '7')),
                        new MethodOutput(intType, '7'))]

        when:
        def result = SingleUnitTestBuilder.createTestMethod(CLASS_UNDER_TEST, methodUnderTest).withTestCases(testCases).build()

        then:
        result.method.getSourceCode() ==
'''
    @Test
    @Parameters({"3, 2, 5",
                 "0, 7, 7"})
    public void test_addNumbers(int a, int b, int expectedResult) {
        int result = Sample.addNumbers(a, b);

        assertThat(result).isEqualTo(expectedResult);
    }
'''
    }

    MethodDeclaration methodDeclarationFromSource(String code) {
        File file = folder.newFile("ClassUnderTest.java")
        file.text = code
        CompilationUnit cu = JavaParser.parse(file)
        cu.findFirst(MethodDeclaration).get()
    }
}
