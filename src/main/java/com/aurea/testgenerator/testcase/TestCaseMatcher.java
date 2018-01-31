package com.aurea.testgenerator.testcase;

import com.aurea.testgenerator.source.Unit;
import one.util.streamex.StreamEx;

public interface TestCaseMatcher {

    StreamEx<TestCase> matches(Unit unit);
    TestCaseType getType();
}
