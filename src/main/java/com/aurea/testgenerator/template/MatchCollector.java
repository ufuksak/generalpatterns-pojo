package com.aurea.testgenerator.template;

import com.aurea.testgenerator.generation.UnitTest;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.source.Unit;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface MatchCollector extends Function<Map<Unit, ? extends Collection<UnitTest>>, Map<Unit, ? extends Collection<PatternMatch>>> {
}
