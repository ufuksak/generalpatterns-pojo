package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import one.util.streamex.StreamEx;

import java.util.function.Function;

@FunctionalInterface
public interface PatternMatcher extends Function<Unit, StreamEx<PatternMatch>> {
}
