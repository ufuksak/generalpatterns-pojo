package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.pattern.PatternMatchImpl;

import java.util.Collections;
import java.util.List;

public abstract class TemplateImpl<T extends PatternMatch> implements TestCaseTemplate {

    private final T match;

    public TemplateImpl(T match) {
        this.match = match;
    }

    public T getMatch() {
        return match;
    }

    public String getMethodName() {
        return ((PatternMatchImpl) getMatch()).getMethodName();
    }

    public List<String> getImports() {
        return Collections.emptyList();
    }

    public List<String> getPreparedClasses() {
        return Collections.emptyList();
    }
}
