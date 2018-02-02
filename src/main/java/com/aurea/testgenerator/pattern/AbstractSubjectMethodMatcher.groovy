package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class AbstractSubjectMethodMatcher extends XPathPatternMatcher {

    @Autowired
    CoverageService coverageService

    @Autowired(required = false)
    JavaParserFacade solver

    Optional<MatchVisitor> newVisitor(Unit unit) {
        shouldBeVisited(unit) ? Optional.ofNullable(new UnitMatchVisitor(unit, solver) {
            @Override
            void visit(final MethodDeclaration n, JavaParserFacade solver) {
                if (shouldMethodBeVisited(unit, n)) {
                    try {
                        Optional<PatternMatch> maybeMatch = matchMethod(unit, n)
                        maybeMatch.ifPresent { matches.add(it) }
                    } catch (Exception e) {
                        logger.error("Failed to visit ${unit.className}.${n.nameAsString}", e)
                    }
                }
            }
        }) : Optional.empty()
    }

    abstract Optional<PatternMatch> matchMethod(Unit unit, MethodDeclaration n)

    boolean shouldBeVisited(Unit unit) {
        true
    }

    boolean shouldMethodBeVisited(Unit unit, MethodDeclaration n) {
        true
    }
}
