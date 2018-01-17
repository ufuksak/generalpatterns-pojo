package com.aurea.testgenerator.template

import com.aurea.testgenerator.config.Imports
import groovy.transform.Canonical

import static com.aurea.testgenerator.template.TestLibrary.*


@Canonical
class TestConfig {

    EnumSet<TestLibrary> availableFrameworks
    NamingConvention namingConvention = new NamingConvention()
    Imports imports = new Imports()

    static TestConfig with(TestLibrary... libraries) {
        EnumSet<TestLibrary> availableFrameworks = EnumSet.noneOf(TestLibrary.class)
        availableFrameworks.addAll(libraries)
        if (!libraries.contains(TestNG) && !libraries.contains(JUnit)) {
            throw new IllegalArgumentException("Should have either TestNG and/or JUnit library!")
        }
        return new TestConfig(availableFrameworks)
    }

    boolean isTestNG() {
        availableFrameworks.contains(TestNG)
    }

    boolean isJUnit() {
        availableFrameworks.contains(JUnit)
    }

    boolean isAssertJ() {
        availableFrameworks.contains(AssertJ)
    }

    boolean isOpenPojo() {
        availableFrameworks.contains(OpenPojo)
    }

    boolean isTestCommons() {
        availableFrameworks.contains(TestCommons)
    }

    String getTestAnnotation() {
        if (isTestNG()) {
            return "org.testng.annotations.Test"
        } else if (isJUnit()) {
            return "org.junit.Test"
        } else {
            return ""
        }
    }

    Class<?> getMockClass() {
        org.mockito.Mockito.class
    }
}
