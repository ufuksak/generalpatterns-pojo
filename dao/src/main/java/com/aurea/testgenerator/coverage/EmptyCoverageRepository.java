package com.aurea.testgenerator.coverage;


import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.MethodCoverage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class EmptyCoverageRepository implements CoverageRepository {
    @Override
    public Optional<ClassCoverage> getClassCoverage(ClassCoverageCriteria query) {
        return Optional.empty();
    }

    @Override
    public Optional<MethodCoverage> getMethodCoverage(MethodCoverageCriteria query) {
        return Optional.empty();
    }

    @Override
    public Collection<MethodCoverage> getMethodCoverages(MethodCoverageCriteria query) {
        return Collections.emptyList();
    }
}
