package com.aurea.testgenerator.coverage

import com.aurea.coverage.unit.MethodCoverage
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class JacocoCoverageRepositorySpec extends Specification {

    def "should correctly set type of method arguments in inner class when type is defined as an inner class of parent class"() {
        when:
        Optional<MethodCoverage> methodCoverage = getMethodCoverage('with-inner-classes.xml',
                'org.example.innerness',
                'Library$Shelf',
                'repair(Library.Librarian)')

        then:
        methodCoverage.isPresent()
        methodCoverage.get().getTotal() == 1
    }

    def "should correctly specify full type of inner classes"() {
        when:
        Optional<MethodCoverage> methodCoverage = getMethodCoverage('with-inner-classes.xml',
                'org.example.innerness',
                'Library',
                'getBookFromTopShelf(Library.Librarian)')

        then:
        methodCoverage.isPresent()
        methodCoverage.get().getTotal() == 1
    }

    def "getting method coverage should select big methods out of identical ones"() {
        when:
        Optional<MethodCoverage> methodCoverage = getMethodCoverage("duplicate-names-jacoco.xml",
                "org.example.duplicatemethodnames",
                "Spacecraft",
                "Colonize(Planet)")

        then:
        methodCoverage.isPresent()
        methodCoverage.get().getTotal() == 2
    }

    Optional<MethodCoverage> getMethodCoverage(String jacocoXmlName, String packageName, String className, String methodName) {
        Path xml = new File(getClass().getResource(jacocoXmlName).file).toPath()
        new JacocoCoverageRepository(xml).getMethodCoverage(MethodCoverageCriteria.of(
                packageName, className, methodName))
    }
}


