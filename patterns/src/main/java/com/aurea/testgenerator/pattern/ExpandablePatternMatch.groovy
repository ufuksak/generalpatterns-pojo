package com.aurea.testgenerator.pattern

import groovy.transform.InheritConstructors
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

import static com.google.common.base.Preconditions.checkNotNull

@InheritConstructors
class ExpandablePatternMatch extends Expando implements PatternMatch {

    ClassDescription description() {
        ClassDescription result = (ClassDescription) getProperty("description")
        return checkNotNull(result, "%s must be provided", "Class description")
    }

    PatternType type() {
        PatternType result = (PatternType) getProperty("type")
        return checkNotNull(result, "%s must be provided", "Type")
    }

    String typeAsString() {
        return type().name
    }

    @Override
    String toString() {
        ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE)
    }
}
