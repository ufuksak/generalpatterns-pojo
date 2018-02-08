package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.xml.XPathEvaluator;
import com.github.generator.xml.NodeToXmlConverter;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public abstract class XPathPatternMatcher implements PatternMatcher {

    protected static final Logger logger = LogManager.getLogger(XPathPatternMatcher.class.getSimpleName());

    @Override
    public StreamEx<PatternMatch> apply(Unit unit) {
        try {
            return newVisitor(unit)
                    .map(visitor -> {
                        visitor.visit();
                        return visitor.matches();
                    }).orElse(StreamEx.empty());
        } catch (Exception e) {
            logger.error("Failed to visit " + unit.getClassName(), e);
            return StreamEx.empty();
        }
    }

    protected abstract Optional<MatchVisitor> newVisitor(Unit unit);
}
