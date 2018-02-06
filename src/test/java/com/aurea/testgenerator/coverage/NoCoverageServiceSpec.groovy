package com.aurea.testgenerator.coverage

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.source.Unit
import com.aurea.util.UnitHelper
import spock.lang.Specification
import spock.lang.Unroll

class NoCoverageServiceSpec extends Specification {
    @Unroll
    def 'coverage test'() {
        setup:
        def service = new NoCoverageService()
        String source = this.getClass().getResource('/com/aurea/testgenerator/coverage/DepositDetailHolderHelpWebControl.java').text
        def unit = UnitHelper.getUnitForCode(source).get()
        def coverage = getMethodCoverage(service, unit, name, index)

        expect:
        coverage.instructionCovered == 0
        coverage.instructionUncovered == 0
        coverage.covered == 0
        coverage.uncovered == loc

        where:
        name       | index | loc | jacoco // jacoco data is just for reference, not used in test
        'toWeb'    | 0     | 27  | 23
        'fromWeb'  | 0     | 2   | 1
        'fromWeb'  | 1     | 1   | 1
        'instance' | 0     | 1   | 1
    }

    private static MethodCoverage getMethodCoverage(CoverageService service, Unit unit, String name, int index) {
        def typeDeclaration = unit.cu.getClassByName('DepositDetailHolderHelpWebControl').get()
        def toWebQuery = MethodCoverageQuery.of(unit, typeDeclaration.getMethodsByName(name)[index])
        service.getMethodCoverage(toWebQuery)
    }
}
