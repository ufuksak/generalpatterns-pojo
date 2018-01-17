package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.SourceFilter;
import com.aurea.testgenerator.source.Unit;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
        Collection<PatternMatch> result = new ArrayList<>();
        for (PatternMatcher visitorPatternMatcher : matchers) {
            result.addAll(visitorPatternMatcher.getMatches(unit));
        }
        return result;
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(matchers);
    }

    public SourceFilter getSourceFilter() {
        SourceFilter filter = path -> false;
        for (PatternMatcher patternMatcher : matchers)
            filter = filter.or(patternMatcher.getSourceFilter());
        return filter;
    }
}
