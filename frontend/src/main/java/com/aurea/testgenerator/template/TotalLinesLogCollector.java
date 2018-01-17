package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.easy.LineCounter;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.pattern.ExpandablePatternMatch;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class TotalLinesLogCollector implements LogCollector {

    private static final Logger logger = LogManager.getLogger(TotalLinesLogCollector.class.getSimpleName());

    private final String name;

    public TotalLinesLogCollector(String name) {
        this.name = name;
    }

    public TotalLinesLogCollector() {
        this("");
    }

    @Override
    public void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        IntStreamEx lineCounters = StreamEx.of(classesToMatches.values()).flatMap(List::stream).select(LineCounter.class).mapToInt(LineCounter::getLines);
        int total = StreamEx.of(classesToMatches.values()).flatMap(List::stream).select(ExpandablePatternMatch.class).mapToInt(epm -> {
                    Object lines = epm.getProperty("lines");
                    return null == lines ? 0 : (Integer) lines;
                }
        ).append(lineCounters).sum();

        int totalNumberOfMatches = StreamEx.of(classesToMatches.values()).mapToInt(List::size).sum();
        long totalNumberOfClasses = StreamEx.of(classesToMatches.keySet()).count();
        logger.info(name + ": total uncovered locs is " + total +
                " in " + totalNumberOfMatches + " matches in "
                + totalNumberOfClasses + " classes");
    }

    @Override
    public String toString() {
        return "# of LOCS";
    }
}
