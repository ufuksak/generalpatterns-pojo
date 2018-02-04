package com.aurea.testgenerator.source

import com.aurea.testgenerator.pattern.PatternMatch
import groovy.transform.Immutable


@Immutable
class UnitWithMatches {
    Unit unit
    List<PatternMatch> matches
}
