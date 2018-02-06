package com.aurea.testgenerator.source

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.MethodCoverageQuery
import com.aurea.testgenerator.pattern.PatternMatch
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
}
