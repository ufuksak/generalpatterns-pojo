package com.aurea.testgenerator

import com.aurea.testgenerator.config.SourceConfig
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.SourceFilters
import com.aurea.testgenerator.testcase.TestCaseMatcher
import com.aurea.testgenerator.testcase.ExpandableTestCase
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.PathUnitSource

import com.aurea.testgenerator.source.Unit
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@ContextConfiguration(classes = [SourceConfig, TestConfig])
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
abstract class MatcherSpecBase<T extends TestCaseMatcher> extends Specification {

    private static final Path PATTERN_CASES = Paths.get(this.getResource("").toURI()).resolve(TestConfig.PATTERN_CASES)

    @Autowired
    T matcher

    @Autowired
    JavaSourceFinder sourceFinder

    abstract String getSampleLocation()

    Unit withUnit(Path name) {
        UnitHelper.getUnit(PATTERN_CASES, PATTERN_CASES.resolve(Paths.get(getSampleLocation()).resolve(name)))
    }

    Unit withUnit(String name) {
        UnitHelper.getUnit(PATTERN_CASES, PATTERN_CASES.resolve(Paths.get(getSampleLocation(), name)))
    }

    Unit withJavaUnit(String name) {
        withUnit(name + ".java")
    }

    Map<String, Unit> withUnits(Path... names) {
        StreamEx.of(Arrays.asList(names)).toMap({it.toFile().name}, {withUnit(it)})
    }

    Map<String, Unit> withUnits(String... names) {
        StreamEx.of(Arrays.asList(names)).toMap({it}, {withUnit(it)})
    }

    ExpandableTestCase getFirstMatch(Unit unit) {
        if (preScans != null) {
            Path root = PATTERN_CASES.resolve(getSampleLocation())
            PathUnitSource unitSource = new PathUnitSource(sourceFinder, root, SourceFilters.empty())
            Stream<Unit> allUnits = unitSource.units(SourceFilters.empty())
            preScans.preScans.each {it.preScan(allUnits)}
        }
        matcher.matches(unit)[0] as ExpandableTestCase
    }
}
