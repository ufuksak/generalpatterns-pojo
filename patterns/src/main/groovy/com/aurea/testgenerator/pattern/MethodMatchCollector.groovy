package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.source.Unit
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component


@Component
class MethodMatchCollector {

    Map<MatchType, List<PatternMatch>> match(StreamEx<Unit> units) {

    }
}
