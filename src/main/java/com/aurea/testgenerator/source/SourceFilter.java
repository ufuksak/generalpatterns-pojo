package com.aurea.testgenerator.source;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface SourceFilter extends Predicate<Path> {

    default SourceFilter or(SourceFilter other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }
}
