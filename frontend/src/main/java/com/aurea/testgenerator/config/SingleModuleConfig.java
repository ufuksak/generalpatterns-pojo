package com.aurea.testgenerator.config;

import com.aurea.testgenerator.coverage.CoverageService;
import com.aurea.testgenerator.coverage.EmptyCoverageService;
import com.aurea.testgenerator.coverage.JacocoCoverageRepository;
import com.aurea.testgenerator.coverage.JacocoCoverageService;
import com.aurea.testgenerator.pattern.PatternMatcher;
import com.aurea.testgenerator.pipeline.PipelineBuilder;
import com.aurea.testgenerator.prescans.PreScans;
import com.aurea.testgenerator.source.PathUnitSource;
import com.aurea.testgenerator.source.SourceFinder;
import com.aurea.testgenerator.source.UnitSource;
import com.aurea.testgenerator.template.MatchCollector;
import com.aurea.testgenerator.template.path.SingleModuleTestNameResolver;
import com.aurea.testgenerator.template.path.TestNameResolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class SingleModuleConfig {

    @Bean
    public TestNameResolver testNameResolver() {
        return new SingleModuleTestNameResolver();
    }

    @Bean
    public CoverageService coverageService(PipelineConfiguration cfg) {
        if (cfg.getJacoco() != null) {
            Path jacocoXml = cfg.getJacoco();
            JacocoCoverageRepository repository = JacocoCoverageRepository.fromFile(jacocoXml);
            return new JacocoCoverageService(repository);
        } else {
            return new EmptyCoverageService();
        }
    }

    @Bean
    public UnitSource unitSource(PipelineConfiguration cfg, SourceFinder sourceFinder) {
        return new PathUnitSource(sourceFinder, cfg.getSrc(), p -> true);
    }

    @Bean
    public JavaParserFacade javaParserFacade(PipelineConfiguration cfg) {
        TypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(cfg.getTestSrc().toFile())
        );
        return JavaParserFacade.get(solver);
    }

    protected abstract Path src();

    protected abstract PatternMatcher patternMatcher();

    protected abstract MatchCollector collector();

    protected List<PipelineBuilder> pipelineBuilders() {
        return Collections.emptyList();
//        return Collections.singletonList(PipelineBuilder
//                .fromSource(srcRoot())
//                .withPreScans(preScans().getPreScans())
//                .withFilter(sourceFilter())
//                .mappingTo(patternMatcher())
//                .collectTo(collector()));
    }
}
