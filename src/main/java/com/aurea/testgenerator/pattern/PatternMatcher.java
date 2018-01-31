package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import one.util.streamex.StreamEx;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Predicate;

public interface PatternMatcher {

    StreamEx<PatternMatch> matches(Unit unit);
    PatternType getType();
}
