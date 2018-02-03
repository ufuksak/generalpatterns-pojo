package com.aurea.testgenerator

import com.aurea.testgenerator.generation.TestNodeMethod
import com.aurea.testgenerator.generation.UnitTestCollector
import com.aurea.testgenerator.generation.UnitTestMergeEngine
import com.aurea.testgenerator.generation.UnitTestMergeResult
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.PatternMatchCollector
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.source.UnitSource
import com.aurea.testgenerator.source.UnitTestWriter
import com.github.javaparser.ast.CompilationUnit
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class Pipeline {

    final UnitSource source
    final PatternMatchCollector collector
    final UnitTestCollector unitTestGenerator
    final SourceFilter sourceFilter
    final UnitTestMergeEngine mergeEngine
    final UnitTestWriter unitTestWriter

    @Autowired
    Pipeline(UnitSource unitSource,
             PatternMatchCollector collector,
             UnitTestCollector unitTestGenerator,
             SourceFilter sourceFilter,
             UnitTestMergeEngine mergeEngine,
             UnitTestWriter writer) {
        this.source = unitSource
        this.collector = collector
        this.unitTestGenerator = unitTestGenerator
        this.sourceFilter = sourceFilter
        this.mergeEngine = mergeEngine
        this.unitTestWriter = writer
    }

    void start() {
        log.info """[$source] ⇒ [$collector] ⇒ [$unitTestGenerator]"""

        log.info "Getting units from $source"
        StreamEx<Unit> filteredUnits = source.units(sourceFilter)

        log.info "Finding matches in ${source.size(sourceFilter)} units"
        Map<Unit, List<PatternMatch>> matchesByUnit = collector.apply(filteredUnits)

        logStats('Matching statistics', matchesByUnit)

        log.info "Building unit tests"
        Map<Unit, List<TestNodeMethod>> unitTestsByUnit = unitTestGenerator.apply(matchesByUnit)

        logStats('Unit tests produced', unitTestsByUnit)

        log.info "Post validation for UnitTest..."
        //TODO: Here we do post validation for UnitTest classes - check that names of fields are unique and other validations
        // we can do before proceeding to creating CUs.

        log.info "Merging UnitTests..."
        Map<Unit, UnitTestMergeResult> merged = EntryStream.of(unitTestsByUnit).mapToValue { k, v -> mergeEngine.merge(k, v) }.toMap()

        log.info "Validation after merge..."
        //TODO: Validate that fields are unique after merge
        Map<Unit, CompilationUnit> testsByUnit = EntryStream.of(merged).mapValues { it.unit }.toMap()

        log.info "Generating .java files..."
        unitTestWriter.write(testsByUnit)
    }

    private static void logStats(String message, Map<Unit, List> unitTestsByUnit) {
        String unitTestStats = EntryStream.of(unitTestsByUnit)
                .mapValues{ it.size() }
                .join(': ', '\r\n\t', '')
                .joining()
        log.info "$message: ${unitTestStats}"
    }
}
