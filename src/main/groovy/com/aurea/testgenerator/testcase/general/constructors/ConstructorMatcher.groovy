package com.aurea.testgenerator.testcase.general.constructors

import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.testcase.AbstractSubjectMethodMatcher
import com.aurea.testgenerator.testcase.MatchVisitor
import com.aurea.testgenerator.testcase.TestCase
import com.aurea.testgenerator.testcase.TestCaseType
import com.aurea.testgenerator.testcase.UnitMatchVisitor
import com.aurea.testgenerator.testcase.XPathTestCaseMatcher
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2


@Log4j2
class ConstructorMatcher extends XPathTestCaseMatcher {

    @Override
    TestCaseType getType() {
        ConstructorTypes.FIELD_ASSIGNMENTS
    }

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
