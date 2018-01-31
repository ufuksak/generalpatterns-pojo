package com.aurea.testgenerator.pattern;

import one.util.streamex.StreamEx;

public interface MatchVisitor {

    void visit();

    StreamEx<PatternMatch> matches();
}
