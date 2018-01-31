package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.google.common.base.Joiner;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class UnitToPatternMatch implements Function<Unit, Collection<PatternMatch>> {

    private final List<PatternMatcher> matchers;

    public UnitToPatternMatch(List<PatternMatcher> matchers) {
        this.matchers = matchers;
    }

    public UnitToPatternMatch(PatternMatcher matcher) {
        this(Collections.singletonList(matcher));
    }

    @Override
    public Collection<PatternMatch> apply(Unit unit) {
        return StreamEx.of(matchers).flatMap(m -> m.apply(unit)).toList();
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(matchers);
    }
}
