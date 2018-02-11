package com.aurea.testgenerator.coverage;


import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;

public class EmptyCoverageService implements CoverageService {
    @Override
    public MethodCoverage getMethodCoverage(MethodCoverageQuery methodCoverageQuery) {
        return MethodCoverage.EMPTY;
    }

    @Override
    public ClassCoverage getTypeCoverage(ClassCoverageQuery classCoverageQuery) {
        return ClassCoverageImpl.EMPTY;
    }
}
