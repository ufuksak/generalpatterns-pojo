package com.aurea.bigcode

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SingleUnitTestBuilderSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    def "creating single test case unit test"() {
        when:
        def result = UnitTestInputDataProvider.createSingleTestCaseIntUnitTest(folder)

        then:
        result.method.getSourceCode() ==
'''
    @Test
    public void test_addNumbers_int() {
        int result = Sample.addNumbers(3, 2);

        assertThat(result).isEqualTo(5);
    }
'''
    }

    def "creating single test case unit test for floating point type"() {
        when:
        def result = UnitTestInputDataProvider.createSingleTestCaseFloatUnitTest(folder)

        then:
        result.method.getSourceCode() ==
'''
    @Test
    public void test_addNumbers_float() {
        float result = Sample.addNumbers(3.1F, 2);

        assertThat(result).isCloseTo(5.1F, Offset.offset(0.001));
    }
'''
    }

    def "creating parametrized unit test"() {
        when:
        def result = UnitTestInputDataProvider.createMultilpeTestCasesIntUnitTest(folder)

        then:
        result.method.getSourceCode() ==
'''
    @Test
    @Parameters({"3, 2, 5",
                 "0, 7, 7"})
    public void test_addNumbers_mult(int a, int b, int expectedResult) {
        int result = Sample.addNumbers(a, b);

        assertThat(result).isEqualTo(expectedResult);
    }
'''
    }
}
