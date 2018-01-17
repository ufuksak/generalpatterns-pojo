package com.aurea.testgenerator.pattern;

public interface MatcherRepository {

    PatternMatcher ofClass(Class<? extends PatternMatcher> patternClass);
}
