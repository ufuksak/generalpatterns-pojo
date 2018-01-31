package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import static com.aurea.testgenerator.source.ParsingUtils.parseJavaClassName;

public final class SourceFilters {
    private SourceFilters() {
    }

    public static SourceFilter empty() {
        return p -> true;
    }

    static SourceFilter nameHasSuffix(String suffix) {
        return p -> parseJavaClassName(p).endsWith(suffix);
    }

    static SourceFilter nameContains(String part) {
        return p -> parseJavaClassName(p).contains(part);
    }

    static SourceFilter nameEquals(String name) {
        return p -> parseJavaClassName(p).equals(name);
    }

    static SourceFilter pathHasPrefix(Path prefix) {
        return path -> path.startsWith(prefix);
    }

    static SourceFilter pathHasPrefix(Path... prefixes) {
        return StreamEx.of(prefixes)
                .map(SourceFilters::pathHasPrefix)
                .reduce(p -> false, SourceFilter::or);
    }

    static SourceFilter hasTest(Path srcRoot, Path testRoot) {
        return p -> {
            Path relativeToSourceRoot = srcRoot.relativize(p);
            Path testFilePath = testRoot.resolve(relativeToSourceRoot);
            return !Files.exists(testFilePath);
        };
    }
}
