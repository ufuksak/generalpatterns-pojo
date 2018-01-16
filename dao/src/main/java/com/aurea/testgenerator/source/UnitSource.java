package com.aurea.testgenerator.source;

import java.util.stream.Stream;

public interface UnitSource {
    Stream<Unit> units(SourceFilter filter);

    long size(SourceFilter filter);
}
