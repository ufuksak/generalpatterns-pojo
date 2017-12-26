package com.aurea.bigcode.source

import com.aurea.bigcode.UnitTest
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

        sb << "public class ${classUnderTest.name}Test {" << lineSeparator()

        def methods = StreamEx.of(unitTests).map { it.method.sourceCode.stripMargin() }.joining()
        sb << methods

        sb << "}" << lineSeparator()

        sb.toString()
    }

    private static StreamEx<String> getUnitTestImports(UnitTest unitTest) {
        StreamEx.of(unitTest.fields)
                .append(unitTest.methodSetup.stream())
                .append(unitTest.classSetup.stream())
                .append(unitTest.method)
                .flatMap { StreamEx.of(it.imports) }
    }

    private static String combineImports(StreamEx<String> imports) {
        imports
                .map { it.trim() }
                .sorted()
                .distinct()
                .joining(lineSeparator())
    }
}
