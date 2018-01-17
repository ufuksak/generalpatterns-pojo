package com.aurea.testgenerator.pattern

import com.aurea.coverage.unit.ClassCoverage
import com.aurea.testgenerator.coverage.ClassCoverageQuery
import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractSubjectClassMatcher extends XPathPatternMatcher {

    @Autowired
    CoverageService coverageService

    @Autowired(required = false)
    JavaParserFacade solver

    MatchVisitor newVisitor(Unit unit) {
        shouldBeVisited(unit) ? new UnitMatchVisitor(unit, solver) {
            @Override
            void visit(final ClassOrInterfaceDeclaration n, JavaParserFacade solver) {
                if (shouldClassBeVisited(unit, n)) {
                    Optional<ExpandablePatternMatch> maybeMatch = matchClass(unit, n)
                    if (maybeMatch.isPresent()) {
                        ExpandablePatternMatch match = maybeMatch.get()
                        if (!match.description)
                            match.description = new ClassDescription(unit)
                        match.class = unit.className
                        match.packagename = unit.packageName
                        if (collectCoverage()) {
                            ClassCoverage coverage = coverageService.getClassCoverage(ClassCoverageQuery.of(unit, n))
                            match.lines = coverage.methodCoverages().mapToInt {
                                it.getUncovered()
                            }.sum()
                        }
                        matches.add(match)
                    }
                }
            }
        } : EmptyMatchVisitor.get()
    }

    abstract Optional<ExpandablePatternMatch> matchClass(Unit unit, ClassOrInterfaceDeclaration n)

    boolean shouldBeVisited(Unit unit) {
        true
    }

    boolean shouldClassBeVisited(Unit unit, ClassOrInterfaceDeclaration n) {
        true
    }

    boolean collectCoverage() {
        true
    }
}
