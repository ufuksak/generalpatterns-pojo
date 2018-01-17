package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;

public class StatsLogCollector implements LogCollector {

    private static final Logger logger = LogManager.getLogger(StatsLogCollector.class.getSimpleName());

    private final String name;

    public StatsLogCollector(String name) {
        this.name = name;
    }

    public StatsLogCollector() {
        this("");
    }

    @Override
    public void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        String classesToMatchesReport = print(classesToMatches);
        logger.info(name + " classes - " + classesToMatches.size() + ", " +
                "matches - " + classesToMatches.values().stream().mapToInt(List::size).sum() + lineSeparator() + classesToMatchesReport);
    }
}
