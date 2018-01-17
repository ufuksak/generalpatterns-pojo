package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;

public interface LogCollector extends MatchCollector {

    Logger logger = LogManager.getLogger(LogCollector.class.getSimpleName());

    default String print(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        StringBuilder builder = new StringBuilder();
        for (ClassDescription cd : classesToMatches.keySet()) {
            List<PatternMatch> matches = classesToMatches.get(cd);
            builder.append(lineSeparator())
                    .append("Matches in class: ")
                    .append(cd.getPackageName())
                    .append(".")
                    .append(cd.getClassName());
            for (PatternMatch pm : matches) {
                builder.append("\t").append(pm).append(lineSeparator());
            }
        }
        return builder.toString();
    }
}
