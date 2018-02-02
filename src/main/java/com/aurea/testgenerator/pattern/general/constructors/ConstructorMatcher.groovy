package com.aurea.testgenerator.pattern.general.constructors

import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.pattern.MatchVisitor
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.UnitMatchVisitor
import com.aurea.testgenerator.pattern.XPathPatternMatcher
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Log4j2
@Component
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
            if (Callability.isCallableFromTests(n) && n.body.empty) {
                matches << new PatternMatch(match: n, type: ConstructorTypes.EMPTY)
            }
        }
    }
}
