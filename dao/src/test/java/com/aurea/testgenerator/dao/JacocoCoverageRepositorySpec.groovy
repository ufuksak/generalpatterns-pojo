package com.aurea.testgenerator.dao

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.coverage.JacocoCoverageRepository
import com.aurea.testgenerator.coverage.MethodCoverageCriteria
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class JacocoCoverageRepositorySpec extends Specification {
    private static final Path JACOCO_SAMPLES

    static {
        URL url = JacocoCoverageRepositorySpec.class.getResource("")
        JACOCO_SAMPLES = Paths.get(url.toURI())
    }

    def "getting method coverage should return present result"() {
        when:
        Optional<MethodCoverage> methodCoverage = getMethodCoverage("duplicate-names-jacoco.xml",
                "com.al6.jtob.data.dao",
                "_RootDAO",
                "initialize()")

        then:
        methodCoverage.isPresent()
        methodCoverage.get().getTotal() == 8
    }

    def "should find coverage for inner class"() {
        when:
        Optional<MethodCoverage> methodCoverage = getMethodCoverage("duplicate-names-jacoco.xml",
                "com.al6.jtob.integration.base",
                '_BaseRootDAO$TransactionPointer',
                'setTransactionRunnable(_BaseRootDAO.TransactionRunnable)')

        then:
        methodCoverage.isPresent()
        methodCoverage.get().getTotal() == 2
    }

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

    static Optional<MethodCoverage> getMethodCoverage(String jacocoXmlName, String packageName, String className, String methodName) {
        new JacocoCoverageRepository(JACOCO_SAMPLES.resolve(jacocoXmlName)).getMethodCoverage(MethodCoverageCriteria.of(
                packageName, className, methodName))
    }
}


