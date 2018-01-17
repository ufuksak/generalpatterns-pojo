package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.PatternMatch;

public class ToStringTemplate implements Template, Comparable<ToStringTemplate> {

    private final String str;

    public ToStringTemplate(PatternMatch patternMatch) {
        str = patternMatch.toString();
    }

    @Override
    public String getName() {
        return "as-string.ftl";
    }

    @Override
    public int compareTo(ToStringTemplate o) {
        return str.compareTo(o.str);
    }
}
