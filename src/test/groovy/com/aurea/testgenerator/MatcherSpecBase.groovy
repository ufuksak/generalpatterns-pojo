package com.aurea.testgenerator

import com.aurea.testgenerator.config.SourceConfig
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.PatternMatcher
import com.aurea.testgenerator.source.JavaSourceFinder
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

@ContextConfiguration(classes = [SourceConfig, TestConfig])
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
abstract class MatcherSpecBase<T extends PatternMatcher> extends Specification {

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
        StreamEx.of(Arrays.asList(names)).toMap({ it.toFile().name }, { withUnit(it) })
    }

    Map<String, Unit> withUnits(String... names) {
        StreamEx.of(Arrays.asList(names)).toMap({ it }, { withUnit(it) })
    }

    PatternMatch getFirstMatch(Unit unit) {
        matcher.apply(unit).first()
    }
}
