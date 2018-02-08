package com.aurea.testgenerator.coverage

import com.aurea.testgenerator.source.UnitWithMatches
import com.github.javaparser.ast.body.ConstructorDeclaration
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicLong

@Component
@Log4j2
class CoverageCollector {
    private CoverageService coverageService
    private AtomicLong totalCoverage

    @Autowired
    CoverageCollector(CoverageService coverageService) {
        this.coverageService = coverageService
        totalCoverage = new AtomicLong()
    }

    void addMatchesCoverage(UnitWithMatches it) {
        def coverage = it.getCoverage(coverageService)
        def classCoverageQuery = ClassCoverageQuery.of(it.unit, it.unit.cu.getClassByName(it.unit.className).get())
        def classLocs = coverageService.getClassCoverage(classCoverageQuery).methodCoverages().mapToInt {
            it.uncovered
        }.sum()
        log.info("$it.unit.fullName: covered $coverage of $classLocs loc")
        totalCoverage.getAndAdd(coverage)
    }

    void logMethodCoverage(UnitWithMatches it) {
        def uncoveredMethods = it.uncoveredMethods.grep(ConstructorDeclaration)
        // grep can be removed once we accommodate "methods" other than constructors
        if (uncoveredMethods) {
            log.info("$it.unit.fullName: uncovered methods:")
            def uncoveredMethodDeclarations = uncoveredMethods
                    *.getDeclarationAsString(true, false)
                    .join('\n\t')
            log.info("\t$uncoveredMethodDeclarations")
        }
    }

    long getTotalCoverage() {
        return totalCoverage.get()
    }
}
