package com.aurea.testgenerator.testcase;

import one.util.streamex.StreamEx;

public interface MatchVisitor {

    void visit();

    StreamEx<TestCase> matches();
}
