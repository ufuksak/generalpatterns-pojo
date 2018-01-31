package com.aurea.testgenerator.prescans;

import com.aurea.testgenerator.source.Unit;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

@FunctionalInterface
public interface PreScan {
    void preScan(Stream<Unit> units);
    default Predicate<Path> getFilter() {
        return p -> true;
    }
}
