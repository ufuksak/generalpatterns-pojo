package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;
import com.github.javaparser.Position;

import java.util.Collections;

public class NoCoverageService implements CoverageService {
    @Override
    public MethodCoverage getMethodCoverage(MethodCoverageQuery methodCoverageQuery) {
        Position from = methodCoverageQuery.getMethod().getBegin().get();
        Position to = methodCoverageQuery.getMethod().getEnd().get();
        int rawSize = to.line - from.line - 1;
        if (rawSize < 0) {
            rawSize = 0;
        }
        return new MethodCoverage(methodCoverageQuery.getMethod().getNameAsString(), 1, 1, 0, rawSize);
    }

    @Override
    public ClassCoverage getClassCoverage(ClassCoverageQuery classCoverageQuery) {
        return new ClassCoverageImpl(classCoverageQuery.getClassOfTheMethod().getNameAsString(), Collections.emptyList());
    }
}
