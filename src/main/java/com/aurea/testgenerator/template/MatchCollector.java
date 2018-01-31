package com.aurea.testgenerator.template;

import com.aurea.testgenerator.testcase.ClassDescription;
import com.aurea.testgenerator.testcase.TestCase;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface MatchCollector extends Consumer<Map<ClassDescription, List<TestCase>>> {
}
