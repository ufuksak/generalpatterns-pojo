package com.aurea.testgenerator.pattern;

import java.util.Collection;
import java.util.Collections;

public class EmptyMatchVisitor implements MatchVisitor {

    private static final EmptyMatchVisitor instance = new EmptyMatchVisitor();

    public static EmptyMatchVisitor get() {
        return instance;
    }

    private EmptyMatchVisitor() {
    }

    @Override
    public void visit() {
    }

    public Collection<PatternMatch> getMatches() {
        return Collections.emptyList();
    }
}
