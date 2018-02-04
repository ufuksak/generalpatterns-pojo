package com.aurea.testgenerator.source

import com.aurea.testgenerator.pattern.PatternMatch
import groovy.transform.Canonical
import groovy.transform.Immutable


@Canonical
class UnitWithMatches {
    Unit unit
    List<PatternMatch> matches
}
