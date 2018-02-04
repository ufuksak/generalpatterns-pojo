package com.aurea.testgenerator.generation

import com.aurea.common.JavaClass
import com.aurea.testgenerator.generation.source.Annotations
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.source.UnitWithMatches
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
class UnitTestGenerator implements Function<UnitWithMatches, Optional<TestUnit>> {

    List<PatternToTest> patternToTests

    @Autowired
    UnitTestGenerator(List<PatternToTest> patternToTests) {
        this.patternToTests = patternToTests
        log.info "Registered patternToTests: $patternToTests"
    }

    @Override
    Optional<TestUnit> apply(UnitWithMatches unitWithMatches) {
        PackageDeclaration pd = unitWithMatches.unit.cu.getPackageDeclaration().get()
        CompilationUnit testCu = new CompilationUnit(pd,
                unitWithMatches.unit.cu.getImports(),
                NodeList.nodeList(newTestClass(unitWithMatches.unit)), null)
        Unit test = new Unit(testCu, new JavaClass(pd.nameAsString, getTestName(unitWithMatches.unit)), null)
        TestUnit testUnit = new TestUnit(unitWithMatches.unit, test)
        StreamEx.of(unitWithMatches.matches).forEach { match ->
            StreamEx.of(patternToTests).forEach { patternToTest ->
                try {
                    patternToTest.accept(match, testUnit)
                } catch (Exception e) {
                    log.error "Failed to generate tests for $match", e
                }
            }
        }
        boolean hasTests = StreamEx.of(testUnit.test.cu.findFirst(ClassOrInterfaceDeclaration).get().methods).anyMatch {
            it.annotations.contains Annotations.TEST
        }
        return hasTests ? Optional.of(testUnit) : Optional.empty()
    }

    @Override
    String toString() {
        'UnitTestGenerator'
    }

    private static ClassOrInterfaceDeclaration newTestClass(Unit unit) {
        new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, getTestName(unit))
    }

    private static String getTestName(Unit unit) {
        unit.fullName + "Test"
    }

}
