package com.aurea.testgenerator.testcase

import com.aurea.testgenerator.source.Unit
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component


@Component
class TestCaseGenerator {

    Map<Unit, List<TestCase>> generate(StreamEx<Unit> units) {
        return Collections.emptyMap()
    }
}
