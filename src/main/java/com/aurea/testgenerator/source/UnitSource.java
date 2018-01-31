package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.nio.file.Path;
import java.util.function.Predicate;

public interface UnitSource {
    StreamEx<Unit> units(Predicate<Path> filter);

    long size(Predicate<Path> filter);
}
