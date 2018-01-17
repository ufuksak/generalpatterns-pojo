package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public abstract class ASTPatternMatcher implements PatternMatcher {

    private static final Logger logger = LogManager.getLogger(ASTPatternMatcher.class.getSimpleName());

    @Override
    public Collection<PatternMatch> getMatches(Unit unit) {
        UnitMatchVisitor visitor = newVisitor(unit);
        try {
            visitor.visit(unit.getCu(), null);
            return visitor.getMatches();
        } catch (Exception e) {
            logger.error("Failed to visit " + unit.getClassName());
            throw e;
        }
    }

    protected abstract UnitMatchVisitor newVisitor(Unit unit);

    @Override
    public PatternType getType() {
        return ClassNamePatternType.of(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
