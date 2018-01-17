package com.aurea.testgenerator.source;

import java.nio.file.Path;
import java.util.function.Predicate;

@FunctionalInterface
public interface SourceFilter extends Predicate<Path> {
}
