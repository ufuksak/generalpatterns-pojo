package com.aurea.testgenerator.coverage

import spock.lang.Specification

class MethodCoverageQuerySpec extends Specification {

    def "static factory method"() {
        expect:
        MethodCoverageCriteria.of(null, null) != null
    }
}
