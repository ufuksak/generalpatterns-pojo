package com.aurea.testgenerator.source;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface UnitSource {
    Stream<Unit> units(Predicate<Path> filter);

    long size(Predicate<Path> filter);
}
