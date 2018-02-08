package com.aurea.testgenerator.source

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.MethodCoverageQuery
import com.aurea.testgenerator.pattern.PatternMatch
import com.github.javaparser.ast.body.CallableDeclaration
import groovy.transform.Canonical
import one.util.streamex.StreamEx

@Canonical
class UnitWithMatches {
    Unit unit
    List<PatternMatch> matches

    def getCoverage(CoverageService coverageService) {
        StreamEx.of(matches)
                .map { it.match }
                .distinct()
                .mapToInt { coverageService.getMethodCoverage(MethodCoverageQuery.of(unit, it)).uncovered }
                .sum()
    }

    Collection<CallableDeclaration> getUncoveredMethods() {
        unit.cu.findAll(CallableDeclaration).findAll { !(it in matches.match) }
    }
}
