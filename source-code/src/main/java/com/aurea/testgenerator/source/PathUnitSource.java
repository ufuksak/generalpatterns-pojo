package com.aurea.testgenerator.source;

import groovy.transform.Memoized;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PathUnitSource implements UnitSource {

    private static final Logger logger = LogManager.getLogger(PathUnitSource.class.getSimpleName());

    private final SourceFinder sourceFinder;
    private final PathToUnitMapper pathToUnitMapper;
    private final SourceFilter filter;
    private final Path root;

    public PathUnitSource(SourceFinder sourceFinder, Path srcRoot, SourceFilter filter) {
        this.sourceFinder = sourceFinder;
        this.pathToUnitMapper = new PathToUnitMapper(srcRoot);
        this.filter = filter;
        this.root = srcRoot;
    }

    @Override
    public Stream<Unit> units(SourceFilter anotherFilter) {
        try {
            return sources(anotherFilter)
                    .map(pathToUnitMapper)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException e) {
            logger.error("Failed to fetch units", e);
            return Stream.empty();
        }
    }

    private StreamEx<Path> sources(SourceFilter anotherFilter) throws IOException {
        return StreamEx.of(sourceFinder.javaClasses(root))
                .parallel()
                .filter(filter.and(anotherFilter));
    }

    @Override
    @Memoized
    public long size(SourceFilter anotherFilter) {
        try {
            return sources(anotherFilter).count();
        } catch (IOException e) {
            logger.error("Failed to fetch units", e);
            return 0;
        }
    }

    @Override
    public String toString() {
        return root + "/**/*.java";
    }
}
