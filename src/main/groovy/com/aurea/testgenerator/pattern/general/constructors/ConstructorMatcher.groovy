package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.pattern.MatchVisitor
import com.aurea.testgenerator.pattern.UnitMatchVisitor
import com.aurea.testgenerator.pattern.XPathPatternMatcher
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2

@Log4j2
class ConstructorMatcher extends XPathPatternMatcher {

    @Override
    protected Optional<MatchVisitor> newVisitor(Unit unit) {
        Optional.of(new ConstructorVisitor(unit))
    }

    static class ConstructorVisitor extends UnitMatchVisitor {
        ConstructorVisitor(Unit unit) {
            super(unit)
        }

        @Override
        void visit(ConstructorDeclaration n, JavaParserFacade arg) {
            log.info "Visited constructor in ${unit.fullName()}"
        }
    }
}
