package com.aurea.testgenerator.generation

import com.aurea.testgenerator.pattern.PatternMatch

import java.util.function.BiConsumer

@FunctionalInterface
interface PatternToTest extends BiConsumer<PatternMatch, TestUnit> {
}
