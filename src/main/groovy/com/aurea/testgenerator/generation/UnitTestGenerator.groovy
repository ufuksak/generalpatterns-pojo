package com.aurea.testgenerator.generation

import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.source.Unit
import org.springframework.stereotype.Component

import java.util.function.Function

@Component
class UnitTestGenerator implements Function<Map<Unit, List<PatternMatch>>, Map<Unit, List<UnitTest>>> {

    @Override
    Map<Unit, List<UnitTest>> apply(Map<Unit, List<PatternMatch>> unitListMap) {
        Collections.emptyMap()
    }

    @Override
    String toString() {
        'UnitTestGenerator'
    }
}
