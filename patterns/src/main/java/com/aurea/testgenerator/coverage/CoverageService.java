package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.MethodCoverage;

public interface CoverageService {

    MethodCoverage getMethodCoverage(MethodCoverageQuery methodCoverageQuery);

    ClassCoverage getClassCoverage(ClassCoverageQuery classCoverageQuery);
}
