package com.aurea.testgenerator.testcase;

import com.aurea.testgenerator.source.Unit;
import com.google.common.base.Joiner;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class UnitToTestCasesMapper implements Function<Unit, Collection<TestCase>> {

    private final List<TestCaseMatcher> matchers;

    public UnitToTestCasesMapper(List<TestCaseMatcher> matchers) {
        this.matchers = matchers;
    }

    public UnitToTestCasesMapper(TestCaseMatcher matcher) {
        this(Collections.singletonList(matcher));
    }

    @Override
    public Collection<TestCase> apply(Unit unit) {
        return StreamEx.of(matchers).flatMap(m -> m.matches(unit)).toList();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(matchers);
    }
}
