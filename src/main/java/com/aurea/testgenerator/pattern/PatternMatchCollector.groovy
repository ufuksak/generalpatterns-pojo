package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.source.Unit
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.function.Function

@Component
@Log4j2
class PatternMatchCollector implements Function<StreamEx<Unit>, Map<Unit, List<PatternMatch>>> {

    List<PatternMatcher> matchers

    @Autowired
    PatternMatchCollector(List<PatternMatcher> matchers) {
        this.matchers = matchers
        log.info "Registered matchers: $matchers"
    }

    @Override
    Map<Unit, List<PatternMatch>> apply(StreamEx<Unit> units) {
        units.toMap({ it }, { unit ->
            StreamEx.of(matchers).flatMap { matcher ->
                matcher.apply(unit)
            }.toList()
        })
    }

    @Override
    String toString() {
        'PatternMatchCollector'
    }
}
