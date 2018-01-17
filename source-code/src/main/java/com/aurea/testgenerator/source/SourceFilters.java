package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import static com.aurea.testgenerator.source.ParsingUtils.parseJavaClassName;

public final class SourceFilters {
    private SourceFilters() {}

    static Predicate<Path> nameHasSuffix(String suffix) {
        return p -> parseJavaClassName(p).endsWith(suffix);
    }

    static Predicate<Path> nameContains(String part) {
        return p -> parseJavaClassName(p).contains(part);
    }

    static Predicate<Path> nameEquals(String name) {
        return p -> parseJavaClassName(p).equals(name);
    }

    static Predicate<Path> pathHasPrefix(Path prefix) {
        return path -> path.startsWith(prefix);
    }

    static Predicate<Path> pathHasPrefix(Path... prefixes) {
        return StreamEx.of(prefixes)
                .map(SourceFilters::pathHasPrefix)
                .reduce(p -> false, Predicate::or);
    }

    static Predicate<Path> hasTest(Path srcRoot, Path testRoot) {
        return p -> {
            Path relativeToSourceRoot = srcRoot.relativize(p);
            Path testFilePath = testRoot.resolve(relativeToSourceRoot);
            return !Files.exists(testFilePath);
        };
    }
}
