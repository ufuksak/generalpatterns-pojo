package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;

import java.util.List;
import java.util.Map;

public interface MatchCollector {

    void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches);
}
