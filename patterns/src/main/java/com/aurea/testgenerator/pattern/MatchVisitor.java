package com.aurea.testgenerator.pattern;

import java.util.Collection;

public interface MatchVisitor {

    void visit();

    Collection<PatternMatch> getMatches();
}
