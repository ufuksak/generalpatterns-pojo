package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.xml.XPathEvaluator;
import com.github.generator.xml.NodeToXmlConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public abstract class XPathPatternMatcher implements PatternMatcher {

    protected static final Logger logger = LogManager.getLogger(XPathPatternMatcher.class.getSimpleName());

    @Autowired
    public NodeToXmlConverter xmlConverter;

    @Autowired
    public XPathEvaluator evaluator;

    @Override
    public Collection<PatternMatch> getMatches(Unit unit) {
        MatchVisitor visitor = newVisitor(unit);
        try {
            visitor.visit();
            return visitor.getMatches();
        } catch (Exception e) {
            logger.error("Failed to visit " + unit.getClassName(), e);
            return Collections.emptyList();
        }
    }

    protected abstract MatchVisitor newVisitor(Unit unit);

    @Override
    public PatternType getType() {
        return ClassNamePatternType.of(this);
    }

    public void setXmlConverter(NodeToXmlConverter xmlConverter) {
        this.xmlConverter = xmlConverter;
    }

    public void setEvaluator(XPathEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
