package com.aurea.testgenerator.pipeline

import com.aurea.testgenerator.IterationLogger
import com.aurea.testgenerator.pattern.PatternMatcher
import com.aurea.testgenerator.pattern.UnitToMatchMapper
import com.aurea.testgenerator.prescans.PreScan
import com.aurea.testgenerator.source.PathUnitSource
import com.aurea.testgenerator.source.SourceFinder
import com.aurea.testgenerator.source.UnitSource
import com.aurea.testgenerator.template.MatchCollector
import groovy.transform.TupleConstructor
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.util.function.Predicate

class PipelineBuilder {

    private final Path src
    private PatternMatcher patternMatcher
    private MatchCollector collector
    private Predicate<Path> filter = { true }

    private PipelineBuilder(Path src) {
        this.src = src
    }

    static PipelineBuilder fromSource(Path src) {
        new PipelineBuilder(src)
    }

    PipelineBuilder mappingTo(PatternMatcher patternMatcher) {
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
        UnitToMatchMapper unitToMatchMapper = new UnitToMatchMapper(patternMatcher)
    }
}
