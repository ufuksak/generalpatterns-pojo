package com.aurea.testgenerator.coverage

import com.aurea.common.JavaClass
import com.aurea.testgenerator.generation.TestGeneratorEvent
import com.aurea.testgenerator.source.Unit
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
@Log4j2
class CoverageCollector implements ApplicationListener<TestGeneratorEvent> {
    private CoverageService coverageService
    private Map<JavaClass, AtomicLong> coverageByUnit = new ConcurrentHashMap<>()
    private Map<JavaClass, Integer> totalByUnit = new HashMap<>()

    @Autowired
    CoverageCollector(CoverageService coverageService) {
        this.coverageService = coverageService
    }

    long getTotalCoverage() {
        return StreamEx.of(coverageByUnit.values()).mapToLong { it.longValue() }.sum()
    }

    @Override
    void onApplicationEvent(TestGeneratorEvent event) {
        def unit = event.unit
        int currentUnitCoverage = incrementUnitCoverage(unit, event)
        Integer classLocs = totalByUnit.computeIfAbsent(unit.javaClass, { javaClass ->
            def typeCoverageQuery = ClassCoverageQuery.of(unit, unit.cu.getType(0))
            coverageService.getTypeCoverage(typeCoverageQuery).methodCoverages().mapToInt {
                it.total
            }.sum()
        })
        log.trace "$unit.fullName: covered $currentUnitCoverage of $classLocs loc"
    }

    private int incrementUnitCoverage(Unit unit, TestGeneratorEvent event) {
        int coverage = coverageService.getMethodCoverage(MethodCoverageQuery.of(unit, event.callable)).uncovered
        AtomicLong atomicCoverage = new AtomicLong(coverage)

        coverageByUnit.merge(unit.javaClass, atomicCoverage, { AtomicLong l1, AtomicLong l2 ->
            l1.addAndGet(l2.longValue())
            l1
        }).intValue()
    }
}
