package com.aurea.testgenerator.common;

import static java.util.Comparator.comparingInt;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import org.springframework.stereotype.Component;

/**
 * LeftoversCollector can be used when your matcher don't cover some cases (yet), and you would like to know,
 * how many of each case is left uncovered.
 * @param <T>
 */
@Component
public class LeftoversCollector<T> {
    private LongAdder total = new LongAdder();
    private Map<T, Integer> leftovers = new ConcurrentHashMap<>();

    public void miss(T type) {
        leftovers.put(type, leftovers.getOrDefault(type, 0) + 1);
    }

    /**
     * Counts total number of items, if clicked for each one
     */
    public void click() {
        total.increment();
    }

    public void click(Long value) {
        total.add(value);
    }

    public String report() {
        int missed = IntStreamEx.of(leftovers.values()).sum();
        String report = String.format("\n\nCurrent coverage: %.2f%%\n", 100 * (1 - missed / total.doubleValue()));
        return EntryStream.of(leftovers)
                .reverseSorted(comparingInt(Entry::getValue))
                .map(entry -> "\t" + entry.getKey().toString() + (entry.getValue() > 1 ? ": " + entry.getValue().toString() : ""))
                .joining("\n") + report;
    }
}
