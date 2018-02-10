package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import static com.aurea.common.ParsingUtils.parseJavaClassName;

public final class SourceFilters {
    private SourceFilters() {
    }

    public static SourceFilter empty() {
        return p -> true;
    }

    public static SourceFilter nameHasSuffix(String suffix) {
        return p -> parseJavaClassName(p).endsWith(suffix);
    }

    public static SourceFilter nameContains(String part) {
        return p -> parseJavaClassName(p).contains(part);
    }

    public static SourceFilter nameEquals(String name) {
        return p -> parseJavaClassName(p).equals(name);
    }

    public static SourceFilter pathHasPrefix(Path prefix) {
        return path -> path.startsWith(prefix);
    }

    public static SourceFilter pathHasPrefix(Path... prefixes) {
        return StreamEx.of(prefixes)
                .map(SourceFilters::pathHasPrefix)
                .reduce(p -> false, SourceFilter::or);
    }

    public static SourceFilter hasTest(Path srcRoot, Path testRoot, UnaryOperator<Path> toTestName) {
        return p -> {
            Path relativeToSourceRoot = srcRoot.relativize(p);
            Path testFilePath = testRoot.resolve(toTestName.apply(relativeToSourceRoot));
            return Files.exists(testFilePath);
        };
    }
}
