package com.aurea.testgenerator.pattern;

import java.util.Collection;
import java.util.Collections;

public class EmptyUnitMatchVisitor extends UnitMatchVisitor implements MatchVisitor {

    private static final EmptyUnitMatchVisitor instance = new EmptyUnitMatchVisitor();

    public static EmptyUnitMatchVisitor get() {
        return instance;
    }

    private EmptyUnitMatchVisitor() {
        super(null);
    }

    @Override
    public void visit() {
    }

    public Collection<PatternMatch> getMatches() {
        return Collections.emptyList();
    }
}
