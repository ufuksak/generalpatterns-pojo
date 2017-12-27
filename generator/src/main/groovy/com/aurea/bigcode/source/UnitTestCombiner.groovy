package com.aurea.bigcode.source

import com.aurea.bigcode.UnitTest
import com.aurea.bigcode.source.runner.JUnitParamsRunnerConfiguration
import com.aurea.bigcode.source.runner.RunnerConfiguration
import com.aurea.testgenerator.source.JavaClass
import one.util.streamex.StreamEx

import static com.aurea.bigcode.source.CodeStyle.lineSeparator
import static com.aurea.bigcode.source.CodeStyle.multilineSeparator

class UnitTestCombiner {
    private JavaClass classUnderTest
    private Collection<UnitTest> unitTests

    private UnitTestCombiner(JavaClass classUnderTest) {
        this.classUnderTest = classUnderTest
        unitTests = []
    }

    static UnitTestCombiner forClass(JavaClass classUnderTest) {
        new UnitTestCombiner(classUnderTest)
    }

    UnitTestCombiner withUnitTests(Collection<UnitTest> unitTests) {
        this.unitTests.addAll(unitTests)
        this
    }

    String combine() {
        def sb = new StringBuilder()

        sb << "package ${classUnderTest.package};" << multilineSeparator()

        def imports = combineImports(StreamEx.of(unitTests).flatMap { getUnitTestImports(it) })
        if (imports) {
            sb << imports << multilineSeparator()
        }

        Set<RunnerConfiguration> runnerConfigurations = StreamEx.of(unitTests).flatMap { getUnitTestRunnerConfigurations(it) }.toSet()
        if (runnerConfigurations.size() == 1 && runnerConfigurations.first() instanceof JUnitParamsRunnerConfiguration) {
            sb << '@RunWith(JUnitParamsRunner.class)' << lineSeparator()
        } else if (runnerConfigurations > 0) {
            throw new IllegalArgumentException("Unsupported runner configurations: $runnerConfigurations")
        }

        sb << "public class ${classUnderTest.name}Test {" << lineSeparator()

        def methods = StreamEx.of(unitTests).map { it.method.sourceCode.stripMargin() }.joining()
        sb << methods

        sb << "}" << lineSeparator()

        sb.toString()
    }

    private static StreamEx<RunnerConfiguration> getUnitTestRunnerConfigurations(UnitTest unitTest) {
        getUnitTestSourceCodeSuppliers(unitTest).flatMap { StreamEx.of(it.runnerConfigurations) }
    }

    private static StreamEx<String> getUnitTestImports(UnitTest unitTest) {
        getUnitTestSourceCodeSuppliers(unitTest).flatMap { StreamEx.of(it.imports) }
    }

    private static StreamEx<SourceCodeSupplier> getUnitTestSourceCodeSuppliers(UnitTest unitTest) {
        StreamEx.of(unitTest.fields)
                .append(unitTest.methodSetup.stream())
                .append(unitTest.classSetup.stream())
                .append(unitTest.method)
    }

    private static String combineImports(StreamEx<String> imports) {
        imports
                .map { it.trim() }
                .sorted()
                .distinct()
                .joining(lineSeparator())
    }
}
