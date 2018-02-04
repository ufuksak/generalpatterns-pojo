package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.source.Unit
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.function.Function

@Component
@Log4j2
class PatternMatchEngine implements Function<Unit, List<PatternMatch>> {

    List<PatternMatcher> matchers

    @Autowired
    PatternMatchEngine(List<PatternMatcher> matchers) {
        this.matchers = matchers
        log.info "Registered matchers: $matchers"
    }

    @Override
    List<PatternMatch> apply(Unit unit) {
        StreamEx.of(matchers)
                .flatMap { matcher -> matcher.apply(unit) }
                .toList()
    }

    @Override
    String toString() {
        'PatternMatchEngine'
    }
}
