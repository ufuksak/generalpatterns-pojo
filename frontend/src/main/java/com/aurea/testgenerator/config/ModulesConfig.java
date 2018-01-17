package com.aurea.testgenerator.config;

import com.aurea.testgenerator.pipeline.Pipeline;
import com.aurea.testgenerator.pipeline.PipelineBuilder;
import com.aurea.testgenerator.source.SourceFinder;
import com.aurea.testgenerator.template.path.TestNameResolver;
import com.aurea.testgenerator.template.path.TestNameResolverImpl;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;
import java.util.List;

public abstract class ModulesConfig {

    protected static final Logger logger = LogManager.getLogger(ModulesConfig.class.getSimpleName());

    @Value("${source.root}")
    protected String root;

    @Autowired
    protected SourceFinder sourceFinder;

    @Bean
    public TestNameResolver testNameResolver() {
        return new TestNameResolverImpl();
    }

    @Bean
    public List<? extends Pipeline> pipelines() {
        return StreamEx.of(pipelineBuilders()).map(builder -> builder.build(sourceFinder)).toList();
    }

    protected abstract List<PipelineBuilder> pipelineBuilders();
}
