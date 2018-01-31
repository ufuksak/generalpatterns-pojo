package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.source.Unit
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

import java.util.function.Function

@Component
class PatternMatchCollector implements Function<StreamEx<Unit>, Map<Unit, List<PatternMatch>>> {
    @Override
    Map<Unit, List<PatternMatch>> apply(StreamEx<Unit> units) {
        Collections.emptyMap()
    }

    @Override
    String toString() {
        'PatternMatchCollector'
    }
}
