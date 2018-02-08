package com.aurea.testgenerator.generation

import com.aurea.common.JavaClass
import com.aurea.testgenerator.generation.source.Annotations
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

import java.util.function.Function

@Component
@Log4j2
class UnitTestGenerator implements Function<Unit, Optional<TestUnit>> {

    List<TestGenerator> generators

    @Autowired
    UnitTestGenerator(List<TestGenerator> generators) {
        this.generators = generators
        log.info "Registered generators: $generators"
    }

    @Override
    Optional<TestUnit> apply(Unit unitUnderTest) {
        PackageDeclaration pd = unitUnderTest.cu.getPackageDeclaration().get()
        ClassOrInterfaceDeclaration testClass = newTestClass(unitUnderTest)
        CompilationUnit testCu = new CompilationUnit(pd,
                unitUnderTest.cu.getImports(),
                NodeList.nodeList(testClass), null)
        Unit test = new Unit(testCu, new JavaClass(pd.nameAsString, getTestName(unitUnderTest)), null)
        TestUnit testUnit = new TestUnit(unitUnderTest, test)
        List<TestGeneratorResult> testGeneratorResults = StreamEx.of(generators).flatMap {
            it.generate(unitUnderTest).stream()
        }.toList()

        //Merge here, currently simply imports
        StreamEx.of(testGeneratorResults).flatMap {it.tests.stream()}.each { testNodeMethod ->
            testNodeMethod.dependency.imports.each { testCu.addImport(it) }
            testClass.addMember(testNodeMethod.md)
        }

        boolean hasTests = testGeneratorResults.any {!it.tests.empty}
        return hasTests ? Optional.of(testUnit) : Optional.empty()
    }

    @Override
    String toString() {
        "$generators"
    }

    private static ClassOrInterfaceDeclaration newTestClass(Unit unit) {
        new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, getTestName(unit))
    }

    private static String getTestName(Unit unit) {
        unit.className + "Test"
    }

}
