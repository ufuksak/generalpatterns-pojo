package com.aurea.testgenerator.prescans;

import com.aurea.testgenerator.source.SourceFilter;
import com.aurea.testgenerator.source.Unit;

import java.util.stream.Stream;

@FunctionalInterface
public interface PreScan {
    void preScan(Stream<Unit> units);
    default SourceFilter getFilter() {
        return p -> true;
    }
}
