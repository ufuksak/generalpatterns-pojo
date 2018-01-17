package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.MethodCoverage;

import java.util.Collection;
import java.util.Optional;

public interface CoverageRepository {
    Optional<ClassCoverage> getClassCoverage(ClassCoverageCriteria query);

    Optional<MethodCoverage> getMethodCoverage(MethodCoverageCriteria query);

    Collection<MethodCoverage> getMethodCoverages(MethodCoverageCriteria query);
}
