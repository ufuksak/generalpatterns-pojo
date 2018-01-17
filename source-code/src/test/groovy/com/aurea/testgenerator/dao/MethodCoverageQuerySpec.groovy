package com.aurea.testgenerator.dao

import com.aurea.testgenerator.coverage.MethodCoverageCriteria
import spock.lang.Specification


class MethodCoverageQuerySpec extends Specification {

    def "static factory method"() {
        expect:
        MethodCoverageCriteria.of(null, null) != null
    }
}
