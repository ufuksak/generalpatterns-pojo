package com.aurea.testgenerator.generation

import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.source.Unit
import groovy.util.logging.Log4j2
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.function.Function
import java.util.stream.Stream

@Component
@Log4j2
class UnitTestCollector implements Function<Map<Unit, List<PatternMatch>>, Map<Unit, List<UnitTest>>> {

    List<UnitTestGenerator> generators

    @Autowired
    UnitTestCollector(List<UnitTestGenerator> generators) {
        this.generators = generators
        log.info "Registered generators: $generators"
    }

    @Override
    Map<Unit, List<UnitTest>> apply(Map<Unit, List<PatternMatch>> matchesByUnit) {
        EntryStream.of(matchesByUnit).mapValues { matches ->
            List<UnitTest> tests = StreamEx.of(matches).flatMap { match ->
                Stream<UnitTest> us = StreamEx.of(generators).flatMap { generator ->
                    generator.apply(match).stream()
                }
                us
            }.toList()
            tests
        }.toMap()
    }

    @Override
    String toString() {
        'UnitTestGenerator'
    }
}
