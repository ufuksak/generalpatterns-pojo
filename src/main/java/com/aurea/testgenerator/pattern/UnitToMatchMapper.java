package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.google.common.base.Joiner;
import one.util.streamex.StreamEx;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class UnitToMatchMapper implements Function<Unit, Collection<PatternMatch>> {

    private final List<PatternMatcher> matchers;

    public UnitToMatchMapper(List<PatternMatcher> matchers) {
        this.matchers = matchers;
    }

    public UnitToMatchMapper(PatternMatcher matcher) {
        this(Collections.singletonList(matcher));
    }

    @Override
    public Collection<PatternMatch> apply(Unit unit) {
        return StreamEx.of(matchers).flatMap(m -> m.matches(unit)).toList();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(matchers);
    }
}
