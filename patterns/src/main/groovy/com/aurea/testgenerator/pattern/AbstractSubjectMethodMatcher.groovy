package com.aurea.testgenerator.pattern

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.MethodCoverageQuery
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

    MatchVisitor newVisitor(Unit unit) {
        shouldBeVisited(unit) ? new UnitMatchVisitor(unit, solver) {
            @Override
            void visit(final MethodDeclaration n, JavaParserFacade solver) {
                if (shouldMethodBeVisited(unit, n)) {
                    try {
                        Optional<? extends PatternMatch> maybeMatch = matchMethod(unit, n)
                        if (maybeMatch.isPresent() ) {
                            PatternMatch match = maybeMatch.get()
                            ClassDescription cd = new ClassDescription(unit)
                            if (match instanceof ExpandablePatternMatch) {
                                match = match as ExpandablePatternMatch
                                match.description = cd
                                match.class = unit.className
                                match.methodName = n.name
                                match.packagename = unit.packageName
                                MethodCoverage coverage = coverageService.getMethodCoverage(MethodCoverageQuery.of(unit, n))
                                match.lines = coverage.getUncovered()
                            }
                            matches.add(match)
                        }
                    } catch (Exception e) {
                        logger.error("Failed to visit ${unit.className}.${n.nameAsString}", e)
                    }
                }
            }
        } : EmptyMatchVisitor.get()
    }

    boolean isNotCovered(Unit unit, MethodDeclaration n) {
        MethodCoverageQuery query = MethodCoverageQuery.of(unit, n)
        MethodCoverage coverage = coverageService.getMethodCoverage(query)
        (coverage.uncovered == 0 && coverage.covered == 0) || coverage.uncovered > 0
    }

    abstract Optional<? extends PatternMatch> matchMethod(Unit unit, MethodDeclaration n)

    boolean shouldBeVisited(Unit unit) {
        true
    }

    boolean shouldMethodBeVisited(Unit unit, MethodDeclaration n) {
        true
    }
}
