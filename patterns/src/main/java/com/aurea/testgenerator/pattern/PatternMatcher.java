package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import java.util.Collection;

public interface PatternMatcher {

    Collection<PatternMatch> getMatches(Unit unit);
    PatternType getType();

    default SourceFilter getSourceFilter() {
        return SourceFilter.empty();
    }
}
