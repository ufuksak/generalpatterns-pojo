package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import one.util.streamex.StreamEx;

import java.util.List;

public class NoCoverageService implements CoverageService {
    @Override
    public MethodCoverage getMethodCoverage(MethodCoverageQuery methodCoverageQuery) {
        return getMethodCoverage(methodCoverageQuery.getMethod());
    }

    private MethodCoverage getMethodCoverage(CallableDeclaration methodDeclaration) {
        List<Node> childNodes = methodDeclaration.getChildNodes();
        long count = NodeLocCounter.count(childNodes);
        assert count < Integer.MAX_VALUE;
        return new MethodCoverage(methodDeclaration.getNameAsString(), 0, 0, 0, (int) count);
    }

    @Override
    public ClassCoverage getClassCoverage(ClassCoverageQuery classCoverageQuery) {
        List<CallableDeclaration> methodDeclarations = classCoverageQuery.getClassOfTheMethod().findAll(CallableDeclaration.class);
        List<MethodCoverage> methodCoverages = StreamEx.of(methodDeclarations)
                .map(this::getMethodCoverage)
                .toList();

        return new ClassCoverageImpl(classCoverageQuery.getClassOfTheMethod().getNameAsString(), methodCoverages);
    }
}
