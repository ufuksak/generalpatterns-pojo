package com.aurea.testgenerator.source;

import static com.aurea.testgenerator.source.ParsingUtils.parseJavaClassName;

import java.nio.file.Path;
import java.util.function.Predicate;
import one.util.streamex.StreamEx;

public interface SourceFilter extends Predicate<Path> {

    static SourceFilter empty() {
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
                .map(SourceFilter::pathHasPrefix)
                .reduce(p -> false, (filtersOr, filter) -> of(filtersOr.or(filter)));
    }

    static SourceFilter of(Predicate<Path> predicate) {
        return predicate::test;
    }

    default SourceFilter or(SourceFilter anotherSourceFilter) {
        return of(or((Predicate<Path>) anotherSourceFilter));
    }

    default SourceFilter and(SourceFilter anotherSourceFilter) {
        return of(and((Predicate<Path>) anotherSourceFilter));
    }
}
