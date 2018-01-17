package com.aurea.testgenerator.config;

import com.aurea.testgenerator.coverage.CoverageRepository;
import com.aurea.testgenerator.coverage.CoverageService;
import com.aurea.testgenerator.coverage.EmptyCoverageRepository;
import com.aurea.testgenerator.coverage.EmptyCoverageService;
import com.aurea.testgenerator.pattern.MatcherRepository;
import com.aurea.testgenerator.pattern.PatternMatcher;
import com.aurea.testgenerator.pipeline.Pipeline;
import com.aurea.testgenerator.pipeline.PipelineBuilder;
import com.aurea.testgenerator.prescans.PreScan;
import com.aurea.testgenerator.prescans.PreScans;
import com.aurea.testgenerator.source.PathUnitSource;
import com.aurea.testgenerator.source.SourceFilter;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class SingleModuleConfig {

    public static final Path OUT = Paths.get("out");

    @Value("${source.root}")
    protected String root;

    @Autowired
    protected SourceFinder sourceFinder;

    @Autowired
    protected MatcherRepository matcherRepository;

    @Autowired
    protected Map<String, PreScan> preScansByName;

    @Bean
    public List<? extends Pipeline> pipelines() {
        return StreamEx.of(pipelineBuilders()).map(builder -> builder.build(sourceFinder)).toList();
    }

    @Bean
    public TestNameResolver testNameResolver() {
        return new SingleModuleTestNameResolver();
    }

    @Bean
    public Path root() {
        return Paths.get(root);
    }

    @Bean
    public Path srcRoot() {
        return root().resolve(src());
    }

    @Bean
    public CoverageService coverageService() {
        return new EmptyCoverageService();
    }

    @Bean
    public CoverageRepository coverageRepository() {
        return new EmptyCoverageRepository();
    }

    @Bean
    public UnitSource unitSource() {
        return new PathUnitSource(sourceFinder, srcRoot(), sourceFilter());
    }

    @Bean
    public JavaParserFacade javaParserFacade() {
        TypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(srcRoot().toFile())
        );
        return JavaParserFacade.get(solver);
    }

    @Bean
    public PreScans preScans() {
        return PreScans.EMPTY;
    }

    @Bean
    public SourceFilter sourceFilter() {
        return p -> true;
    }

    @Bean
    public String projectName() {
        return "Unknown";
    }

    protected abstract Path src();

    protected abstract Class<? extends PatternMatcher> matcherClass();

    protected abstract MatchCollector collector();

    protected List<PipelineBuilder> pipelineBuilders() {
        return Collections.singletonList(PipelineBuilder
                .fromSource(srcRoot())
                .withPreScans(preScans().getPreScans())
                .withFilter(sourceFilter())
                .mappingTo(matcherRepository.ofClass(matcherClass()))
                .collectTo(collector()));
    }
}
