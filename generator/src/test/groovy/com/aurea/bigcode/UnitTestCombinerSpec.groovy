package com.aurea.bigcode

import com.aurea.bigcode.source.UnitTestCombiner
import com.aurea.testgenerator.source.JavaClass
import spock.lang.Specification

class UnitTestCombinerSpec extends Specification {

    def "combine three unit tests together"() {
        setup:
        List<UnitTest> unitTests = []
        unitTests << UnitTestInputDataProvider.createSingleTestCaseIntUnitTest()
        unitTests << UnitTestInputDataProvider.createSingleTestCaseFloatUnitTest()
        unitTests << UnitTestInputDataProvider.createMultilpeTestCasesIntUnitTest()

        when:
        def result = UnitTestCombiner.forClass(new JavaClass('com.aurea.sample.Sample')).withUnitTests(unitTests).combine()

        then:
        result ==
'''package com.aurea.sample;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class SampleTest {

    @Test
    public void test_addNumbers_int() {
        int result = Sample.addNumbers(3, 2);

        assertThat(result).isEqualTo(5);
    }

    @Test
    public void test_addNumbers_float() {
        float result = Sample.addNumbers(3.1F, 2);

        assertThat(result).isCloseTo(5.1F, Offset.offset(0.001F));
    }

    @Test
    @Parameters({"3, 2, 5",
                 "0, 7, 7"})
    public void test_addNumbers_mult(int a, int b, int expectedResult) {
        int result = Sample.addNumbers(a, b);

        assertThat(result).isEqualTo(expectedResult);
    }
}
'''
    }
}
