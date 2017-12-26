package com.aurea.bigcode

import com.aurea.bigcode.source.UnitTestCombiner
import com.aurea.testgenerator.source.JavaClass
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class UnitTestCombinerSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    def "combine three unit tests together"() {
        setup:
        List<UnitTest> unitTests = []
        unitTests << UnitTestInputDataProvider.createSingleTestCaseIntUnitTest(folder)
        unitTests << UnitTestInputDataProvider.createSingleTestCaseFloatUnitTest(folder)
        unitTests << UnitTestInputDataProvider.createMultilpeTestCasesIntUnitTest(folder)

        when:
        def result = UnitTestCombiner.forClass(new JavaClass('com.aurea.sample.Sample')).withUnitTests(unitTests).combine()

        then:
        result ==
'''
'''
    }
}
