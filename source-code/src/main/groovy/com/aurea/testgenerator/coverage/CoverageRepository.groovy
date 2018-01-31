package com.aurea.testgenerator.coverage

import com.aurea.coverage.unit.ClassCoverage
import com.aurea.coverage.unit.MethodCoverage


interface CoverageRepository {
    Optional<ClassCoverage> getClassCoverage(ClassCoverageCriteria query)

    Optional<MethodCoverage> getMethodCoverage(MethodCoverageCriteria query)

    Collection<MethodCoverage> getMethodCoverages(MethodCoverageCriteria query)
}
