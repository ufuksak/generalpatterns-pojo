package com.aurea.testgenerator.pipeline

import com.aurea.testgenerator.testcase.TestCaseMatcher
import com.aurea.testgenerator.testcase.UnitToTestCasesMapper
import com.aurea.testgenerator.source.PathUnitSource
import com.aurea.testgenerator.source.SourceFinder
import com.aurea.testgenerator.template.MatchCollector

import java.nio.file.Path
import java.util.function.Predicate

class PipelineBuilder {

    private final Path src
    private TestCaseMatcher patternMatcher
    private MatchCollector collector
    private Predicate<Path> filter = { true }

    private PipelineBuilder(Path src) {
        this.src = src
    }

    static PipelineBuilder fromSource(Path src) {
        new PipelineBuilder(src)
    }

    PipelineBuilder mappingTo(TestCaseMatcher patternMatcher) {
        this.patternMatcher = patternMatcher
        this
    }

    PipelineBuilder collectTo(MatchCollector collector) {
        this.collector = collector
        this
    }

    PipelineBuilder withFilter(Predicate<Path> filter) {
        this.filter = filter
        this
    }

    void build(SourceFinder sourceFinder) {
        PathUnitSource unitSource = new PathUnitSource(sourceFinder, src, filter)
        UnitToTestCasesMapper unitToMatchMapper = new UnitToTestCasesMapper(patternMatcher)
    }
}
