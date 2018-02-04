package com.aurea.testgenerator

import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.UnitTestMergeEngine
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.PatternMatchEngine
import com.aurea.testgenerator.source.*
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class Pipeline {

    final UnitSource source
    final PatternMatchEngine patternMatchEngine
    final UnitTestGenerator unitTestGenerator
    final SourceFilter sourceFilter
    final UnitTestMergeEngine mergeEngine
    final UnitTestWriter unitTestWriter

    @Autowired
    Pipeline(UnitSource unitSource,
             PatternMatchEngine patternMatchEngine,
             UnitTestGenerator unitTestGenerator,
             SourceFilter sourceFilter,
             UnitTestMergeEngine mergeEngine,
             UnitTestWriter writer) {
        this.source = unitSource
        this.patternMatchEngine = patternMatchEngine
        this.unitTestGenerator = unitTestGenerator
        this.sourceFilter = sourceFilter
        this.mergeEngine = mergeEngine
        this.unitTestWriter = writer
    }

    void start() {
        log.info """[$source] ⇒ [$patternMatchEngine] ⇒ [$unitTestGenerator]"""

        log.info "Getting units from $source"
        StreamEx<Unit> filteredUnits = source.units(sourceFilter)

        log.info "Finding matches in ${source.size(sourceFilter)} units"
        filteredUnits.map {
            List<PatternMatch> matches = patternMatchEngine.apply(it)
            log.info "Found ${matches.size()} in ${it}"
            new UnitWithMatches(it, matches)
        }.filter {
            if (it.matches.empty) {
                log.info "Skipping $it.unit since no patterns found in it"
                return false
            }
            return true
        }.map {
            Optional<TestUnit> maybeTestUnit = unitTestGenerator.apply(it)
            if (!maybeTestUnit.present) {
                log.info "Skipping ${it.unit} since no tests were generated for it"
            }
            maybeTestUnit
        }.filter {
            it.present
        }.map{
            it.get()
        }.each {
            unitTestWriter.write(it)
        }
    }
}
