package com.aurea.testgenerator.testcase

import groovy.transform.InheritConstructors
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

import static com.google.common.base.Preconditions.checkNotNull

@InheritConstructors
class ExpandableTestCase extends Expando implements TestCase {

    ClassDescription description() {
        ClassDescription result = (ClassDescription) getProperty("description")
        return checkNotNull(result, "%s must be provided", "Class description")
    }

    TestCaseType type() {
        TestCaseType result = (TestCaseType) getProperty("type")
        return checkNotNull(result, "%s must be provided", "Type")
    }

    String typeAsString() {
        return type().name()
    }

    @Override
    String toString() {
        ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE)
    }
}
