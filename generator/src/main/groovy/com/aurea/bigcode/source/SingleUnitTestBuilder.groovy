package com.aurea.bigcode.source

import com.aurea.bigcode.TestCase
import com.aurea.bigcode.UnitTest
import com.aurea.bigcode.source.runner.JUnitParamsRunnerConfiguration
import com.aurea.testgenerator.source.JavaClass
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.util.logging.Log4j2

import static BasicSourceCodeSupplier.from
import static com.aurea.bigcode.source.CodeStyle.lineSeparator
import static com.aurea.bigcode.source.imports.Imports.*
import static java.util.Objects.requireNonNull

@Log4j2
class SingleUnitTestBuilder {
    MethodDeclaration methodUnderTest
    JavaClass classUnderTest
    List<TestCase> testCases
    String testNameSuffix

    private SingleUnitTestBuilder(JavaClass classUnderTest, MethodDeclaration methodUnderTest) {
        this.classUnderTest = classUnderTest
        this.methodUnderTest = methodUnderTest
        testCases = []
        testNameSuffix = ''
    }

    static SingleUnitTestBuilder createTestMethod(JavaClass classUnderTest, MethodDeclaration methodUnderTest) {
        new SingleUnitTestBuilder(requireNonNull(classUnderTest), requireNonNull(methodUnderTest))
    }

    SingleUnitTestBuilder withTestCase(TestCase testCase) {
        testCases.add(requireNonNull(testCase))
        this
    }

    SingleUnitTestBuilder withTestCases(Collection<TestCase> testCase) {
        testCases.addAll(testCase)
        this
    }

    SingleUnitTestBuilder withSuffix(String testNameSuffix) {
        this.testNameSuffix = testNameSuffix ? "_$testNameSuffix" : ''
        this
    }

    UnitTest build() {
        assert testCases
        testCases.size() == 1 ? buildSingleTestCase() : buildParametrizedUnitTest()
    }


    UnitTest buildSingleTestCase() {
        def input = testCases.first().input
        def output = testCases.first().output
        log.debug "Building single test for $methodUnderTest.nameAsString. " +
                "Expecting output $output for input $input"

        def assertion = Assertions.createPrimitiveAssertion(output.type, from('result'), from(output.result))
        def methodSourceCode = """
    @Test
    public void ${createTestName()}() {
        ${output.type.toString()} result = ${classUnderTest.name}.${methodUnderTest.nameAsString}(${input.values.snippet.join(', ')});

        ${assertion.sourceCode}
    }
"""

        BasicSourceCodeSupplier method = from(methodSourceCode)
                .addDependencies(assertion)
                .addImports(JUNIT_TEST)

        new UnitTest(method: method)
    }

    UnitTest buildParametrizedUnitTest() {
        def outputType = testCases.first().output.type
        def inputParamsString = methodUnderTest.parameters.collect { "${it.type.toString() } ${it.name.toString()}" }.join(', ')
        def methodCallArgumentsString = methodUnderTest.parameters.collect { it.name.toString() }.join(', ')
        log.debug "Building parametrized unit test for $methodUnderTest.nameAsString. " +
                "Tested test cases: $testCases"

        def assertion = Assertions.createPrimitiveAssertion(outputType, from('result'), from('expectedResult'))

        def methodSourceCode = """
    @Test
    @Parameters({${testCases.collect { "\"${it.input.values.snippet.join(', ')}, $it.output.result\"" }.join(",${lineSeparator(1)}             ")}})
    public void ${createTestName()}($inputParamsString, $outputType expectedResult) {
        $outputType result = ${classUnderTest.name}.${methodUnderTest.nameAsString}($methodCallArgumentsString);

        ${assertion.sourceCode}
    }
"""

        BasicSourceCodeSupplier method = from(methodSourceCode)
                .addDependencies(assertion)
                .addImports(JUNIT_TEST, JUNITPARAMS_PARAMETERS, JUNIT_RUNWITH, JUNITPARAMS_JUNITPARAMSRUNNER)
                .addRunnerConfigurations(JUnitParamsRunnerConfiguration.createJUnitParamsRunnerConfiguration())

        new UnitTest(method: method)
    }

    private createTestName() {
        "test_${methodUnderTest.nameAsString}$testNameSuffix"
    }
}
