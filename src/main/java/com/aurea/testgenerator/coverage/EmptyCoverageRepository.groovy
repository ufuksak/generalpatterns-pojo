package com.aurea.testgenerator.coverage

import com.aurea.coverage.unit.ClassCoverage
import com.aurea.coverage.unit.MethodCoverage


class EmptyCoverageRepository implements CoverageRepository {
    @Override
    Optional<ClassCoverage> getClassCoverage(ClassCoverageCriteria query) {
        return Optional.empty()
    }

    @Override
    Optional<MethodCoverage> getMethodCoverage(MethodCoverageCriteria query) {
        return Optional.empty()
    }

    @Override
    Collection<MethodCoverage> getMethodCoverages(MethodCoverageCriteria query) {
        return Collections.emptyList()
    }
}
