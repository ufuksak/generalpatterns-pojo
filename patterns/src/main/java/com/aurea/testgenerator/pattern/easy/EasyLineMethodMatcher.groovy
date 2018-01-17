package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.pattern.AbstractSubjectMethodMatcher
import com.aurea.testgenerator.pattern.ExpandablePatternMatch
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.stereotype.Component

@Component
class EasyLineMethodMatcher extends AbstractSubjectMethodMatcher {

    JavaParserFacade facade

    EasyLineMethodMatcher() {
    }

    EasyLineMethodMatcher(CoverageService coverageService, JavaParserFacade facade) {
        this.coverageService = coverageService
        this.facade = facade
    }


    @Override
    Optional<ExpandablePatternMatch> matchMethod(Unit unit, MethodDeclaration n) {
        for (EasyLine easyLoc : EasyLine.values()) {
            if (easyLoc.is(n, facade)) {
                return Optional.of(new EasyLinePatternMatch(easyLoc))
            }
        }
        Optional.empty()
    }
}
