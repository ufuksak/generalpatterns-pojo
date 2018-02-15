package com.aurea.testgenerator.source;

import com.aurea.testgenerator.config.ProjectConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import groovy.transform.Memoized;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

@Component
public class PathUnitSource implements UnitSource {

    private static final Logger logger = LogManager.getLogger(PathUnitSource.class.getSimpleName());

    private final SourceFinder sourceFinder;
    private final PathToUnitMapper pathToUnitMapper;
    private final SourceFilter filter;
    private final JavaSymbolSolver solver;
    private final Path root;

    public PathUnitSource(SourceFinder sourceFinder, ProjectConfiguration cfg, SourceFilter filter, JavaSymbolSolver solver) {
        this.sourceFinder = sourceFinder;
        this.pathToUnitMapper = new PathToUnitMapper(cfg.getSrcPath());
        this.filter = filter;
        this.root = cfg.getSrcPath();
        this.solver = solver;
    }

    @Override
    public StreamEx<Unit> units(Predicate<Path> anotherFilter) {
        try {
            return sources(anotherFilter)
                    .map(pathToUnitMapper)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::injectSolver);
        } catch (IOException e) {
            logger.error("Failed to fetch units", e);
            return StreamEx.empty();
        }
    }

    private StreamEx<Path> sources(Predicate<Path> anotherFilter) throws IOException {
        return sourceFinder.javaClasses().parallel().filter(filter.and(anotherFilter));
    }

    private Unit injectSolver(Unit unit) {
        solver.inject(unit.getCu());
        return unit;
    }

    @Override
    @Memoized
    public long size(Predicate<Path> anotherFilter) {
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
