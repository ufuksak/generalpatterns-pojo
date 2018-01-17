package com.aurea.testgenerator.template

import com.aurea.testgenerator.pattern.ExpandablePatternMatch

class ExpandableTemplate implements Template {

    final String name
    final ExpandablePatternMatch match

    ExpandableTemplate(ExpandablePatternMatch match, String name) {
        this.match = match
        this.name = name
    }
}
