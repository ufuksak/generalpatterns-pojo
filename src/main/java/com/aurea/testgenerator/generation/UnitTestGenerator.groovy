package com.aurea.testgenerator.generation

import com.aurea.common.JavaClass
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class UnitTestGenerator {

    List<TestGenerator> generators

    @Autowired
    UnitTestGenerator(List<MethodLevelTestGenerator> generators) {
        this.generators = generators
        log.info "Registered generators: $generators"
    }

    Optional<TestUnit> tryGenerateTest(Unit unitUnderTest) {
        PackageDeclaration pd = unitUnderTest.cu.getPackageDeclaration().get()
        ClassOrInterfaceDeclaration testClass = newTestClass(unitUnderTest)
        CompilationUnit testCu = new CompilationUnit(pd,
                unitUnderTest.cu.getImports(),
                NodeList.nodeList(testClass), null)
        Unit test = new Unit(testCu, new JavaClass(pd.nameAsString, getTestName(unitUnderTest)), null)
        TestUnit testUnit = new TestUnit(test)
        List<TestGeneratorResult> testGeneratorResults = StreamEx.of(generators).flatMap {
            it.generate(unitUnderTest).stream()
        }.toList()

        testUnit.addDependencies(StreamEx.of(testGeneratorResults).flatMap { it.tests.stream() }.toList())

        StreamEx.of(testGeneratorResults).flatMap { it.tests.stream() }.each { testNodeMethod ->
            testUnit.addTest(testNodeMethod.node)
        }

        boolean hasTests = testGeneratorResults.any { it.tests }
        return hasTests ? Optional.of(testUnit) : Optional.empty()
    }

    @Override
    String toString() {
        generators
    }

    private static ClassOrInterfaceDeclaration newTestClass(Unit unit) {
        new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, getTestName(unit))
    }

    private static String getTestName(Unit unit) {
        unit.className + "Test"
    }

}
