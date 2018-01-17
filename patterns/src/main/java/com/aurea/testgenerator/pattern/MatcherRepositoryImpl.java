package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.google.common.collect.ImmutableMap;
import one.util.streamex.EntryStream;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class MatcherRepositoryImpl implements MatcherRepository {

    private static final PatternMatcher EMPTY_MATCHER = new PatternMatcher() {
        @Override
        public Collection<PatternMatch> getMatches(Unit unit) {
            return Collections.emptyList();
        }

        @Override
        public PatternType getType() {
            return ClassNamePatternType.of(this);
        }

        @Override
        public String toString() {
            return "EMPTY_MATCHER";
        }
    };

    @Autowired
    private Map<String, PatternMatcher> matchers;

    private ImmutableMap<Class<? extends PatternMatcher>, PatternMatcher> matchersToClasses;

    @PostConstruct
    public void reMapToPattern() {
        matchersToClasses = ImmutableMap.copyOf(EntryStream.of(matchers).values().toMap(
                PatternMatcher::getClass, Function.identity()));

    }

    @Override
    public PatternMatcher ofClass(Class<? extends PatternMatcher> patternClass) {
        return matchersToClasses.getOrDefault(patternClass, EMPTY_MATCHER);
    }
}
