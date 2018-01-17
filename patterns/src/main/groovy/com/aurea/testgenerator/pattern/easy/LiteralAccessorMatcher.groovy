package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.stereotype.Component

import static com.aurea.testgenerator.ast.ASTNodeUtils.findChildOf

@Component
class LiteralAccessorMatcher extends AccessorMatcher {

    LiteralResolver resolver = new LiteralResolver()

    LiteralAccessorMatcher() {
        super()
    }

    LiteralAccessorMatcher(CoverageService coverageService, JavaParserFacade javaParserFacade) {
        super(coverageService, javaParserFacade)
    }

    @Override
    Optional<? extends AccessorMatch> createMatch(Unit unit, MethodDeclaration n, JavaParserFacade facade) {
        ReturnStmt returnStmt = findChildOf(ReturnStmt.class, n).get()
        Optional<String> expression = resolver.tryFindLiteralExpression(returnStmt)
        return expression.map { it -> new LiteralAccessorMatch(unit, n, it) }
    }
}