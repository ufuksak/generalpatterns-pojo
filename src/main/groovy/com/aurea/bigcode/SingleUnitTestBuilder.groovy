package com.aurea.bigcode

import com.aurea.bigcode.source.Assertions
import com.aurea.bigcode.source.Imports
import com.aurea.testgenerator.source.JavaClass
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.util.logging.Log4j2

import static com.aurea.bigcode.source.BasicSourceCodeSupplier.from
import static java.util.Objects.requireNonNull

@Log4j2
class SingleUnitTestBuilder {
    MethodDeclaration methodUnderTest
    JavaClass classUnderTest
    List<TestCase> testCases

    private SingleUnitTestBuilder(JavaClass classUnderTest, MethodDeclaration methodUnderTest) {
        this.classUnderTest = classUnderTest
        this.methodUnderTest = methodUnderTest
        testCases = []
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

    TestMethod build() {
        assert testCases
        testCases.size() == 1 ? buildSingleTestCase() : buildParametrizedUnitTest()
    }


    TestMethod buildSingleTestCase() {
        def input = testCases.first().input
        def output = testCases.first().output
        log.debug "Building single test for $methodUnderTest.nameAsString. " +
                "Expecting output $output for input $input"
        def methodSourceCode = """
    @Test
    public void test_${methodUnderTest.nameAsString}() {
        ${output.type.toString()} result = ${classUnderTest.name}.${methodUnderTest.nameAsString}(${input.values.snippet.join(', ')});

        ${Assertions.createPrimitiveAssertion(output.type, from('result'), from(output.result)).sourceCode}
    }
"""

        new TestMethod(method: from(methodSourceCode).addImports(Imports.JUNIT_TEST))
    }

    TestMethod buildParametrizedUnitTest() {
        def outputType = testCases.first().output.type
        def inputParamsString = methodUnderTest.parameters.collect { "${it.type.toString() } ${it.name.toString()}" }.join(', ')
        def methodCallArgumentsString = methodUnderTest.parameters.collect { it.name.toString() }.join(', ')
        log.debug "Building parametrized unit test for $methodUnderTest.nameAsString. " +
                "Tested test cases: $testCases"
        def methodSourceCode = """
    @Test
    @Parameters({${testCases.collect { "\"${it.input.values.snippet.join(', ')}, $it.output.result\"" }.join(',\n                 ')}})
    public void test_${methodUnderTest.nameAsString}($inputParamsString, $outputType expectedResult) {
        $outputType result = ${classUnderTest.name}.${methodUnderTest.nameAsString}($methodCallArgumentsString);

        ${Assertions.createPrimitiveAssertion(outputType, from('result'), from('expectedResult')).sourceCode}
    }
"""

        new TestMethod(method: from(methodSourceCode).addImports(Imports.JUNIT_TEST, Imports.JUNITPARAMS_PARAMETERS))
    }
}
