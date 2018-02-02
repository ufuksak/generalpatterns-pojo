package com.aurea.testgenerator.generation

import com.aurea.testgenerator.pattern.PatternMatch

import java.util.function.Function


interface UnitTestGenerator extends Function<PatternMatch, List<TestNodeMethod>> {
}