package com.aurea.testgenerator.pattern.easy

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.JacocoCoverageService
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.xml.XPathEvaluatorImpl
import com.github.generator.xml.Converters
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import spock.lang.Specification

import javax.xml.xpath.XPathFactory

import static com.aurea.testgenerator.UnitHelper.getUnitForCode

abstract class AccesorMatcherBaseSpec<T extends AccessorMatch> extends Specification {
    JacocoCoverageService coverageService = Mock(JacocoCoverageService)
    AccessorMatcher matcher

    def setup() {
        coverageService.getMethodCoverage(_) >> new MethodCoverage("has-coverage", 1, 1, 1, 1)
    }

    protected T onFooClassCode(String code) {
        File srcDir = File.createTempDir("java-parser", "test-cases")
        srcDir.deleteOnExit()
        new File(srcDir, "Foo.java").with {
            write(code)
        }
        JavaParserFacade facade = JavaParserFacade.get(
                new CombinedTypeSolver(
                        new ReflectionTypeSolver(),
                        new JavaParserTypeSolver(srcDir)))

        matcher = newMatcher(coverageService, facade)
        matcher.xmlConverter = Converters.newConverter()
        matcher.evaluator = new XPathEvaluatorImpl(XPathFactory.newInstance().newXPath())

        Collection<PatternMatch> matches = matcher.getMatches(getUnitForCode(code).get())
        ++matches.iterator() as T
    }

    abstract AccessorMatcher newMatcher(CoverageService coverageService, JavaParserFacade facade)
}
