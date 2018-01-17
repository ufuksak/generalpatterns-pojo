package com.aurea.testgenerator;

import com.aurea.testgenerator.coverage.CoverageRepository;
import com.aurea.testgenerator.coverage.CoverageService;
import com.aurea.testgenerator.coverage.EmptyCoverageRepository;
import com.aurea.testgenerator.coverage.EmptyCoverageService;
import com.aurea.testgenerator.template.MatchCollector;
import com.aurea.testgenerator.template.path.TestNameResolver;
import com.aurea.testgenerator.template.path.TestNameResolverImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    public static final String PATTERN_CASES = "cases";

    @Bean
    public TestNameResolver testNameResolver() {
        return new TestNameResolverImpl();
    }

    @Bean
    public MatchCollector testGenerator() {
        return mock(MatchCollector.class);
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
    public String projectName() {
        return "Test project";
    }

    @Bean
    public Path srcRoot() throws URISyntaxException {
        Path classFilePath = Paths.get(this.getClass().getResource("").toURI());
        return classFilePath.resolve(PATTERN_CASES);
    }
}
