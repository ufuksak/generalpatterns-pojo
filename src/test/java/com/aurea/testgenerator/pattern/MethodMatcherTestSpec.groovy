package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.UnitHelper
import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.xml.XPathEvaluator
import com.aurea.testgenerator.xml.XPathEvaluatorImpl
import com.github.generator.xml.Converters
import com.github.generator.xml.NodeToXmlConverter
import com.github.javaparser.ast.body.MethodDeclaration
import spock.lang.Specification

import javax.xml.xpath.XPathFactory
import java.lang.reflect.ParameterizedType

abstract class MethodMatcherTestSpec<T extends AbstractSubjectMethodMatcher> extends Specification {

    protected T matcher
    protected CoverageService coverageService = Mock()
    protected NodeToXmlConverter xmlConverter = Converters.newConverter()
    protected XPathEvaluator xPathEvaluator = new XPathEvaluatorImpl(XPathFactory.newInstance().newXPath())

    def setup() {
        matcher = (T)((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance()
        matcher.coverageService = coverageService
        matcher.xmlConverter = xmlConverter
        matcher.evaluator = xPathEvaluator
    }

    protected Optional<? extends PatternMatch> runOnMethod(String methodCode) {
        Optional<Unit> maybeUnit = UnitHelper.getUnitForMethod(methodCode)
        Unit unit = maybeUnit.orElseThrow{ throw new IllegalArgumentException("Faled to parse code: $methodCode")}
        matcher.matchMethod(unit, unit.cu.getNodesByType(MethodDeclaration).first())
    }

    protected Optional<? extends PatternMatch> runOnClass(String classCode) {
        Optional<Unit> maybeUnit = UnitHelper.getUnitForCode(classCode)
        Unit unit = maybeUnit.orElseThrow{ throw new IllegalArgumentException("Faled to parse code: $classCode")}
        matcher.matchMethod(unit, unit.cu.getNodesByType(MethodDeclaration).first())
    }


}
