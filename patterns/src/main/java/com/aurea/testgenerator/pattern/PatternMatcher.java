package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Predicate;

public interface PatternMatcher {

    Collection<PatternMatch> getMatches(Unit unit);
    PatternType getType();

    default Predicate<Path> getSourceFilter() {
        return p -> true;
    }
}
