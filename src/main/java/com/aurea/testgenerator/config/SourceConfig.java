package com.aurea.testgenerator.config;

import com.aurea.testgenerator.coverage.CoverageService;
import com.aurea.testgenerator.coverage.EmptyCoverageService;
import com.aurea.testgenerator.source.JavaSourceFinder;
import com.aurea.testgenerator.source.PathUnitSource;
import com.aurea.testgenerator.source.SourceFilter;
import com.aurea.testgenerator.source.UnitSource;
import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
public class SourceConfig {

    @Autowired
    ProjectConfiguration cfg;

    @Bean
    public NodeToXmlConverter xmlConverter() {
        return Converters.newConverter();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

    @Bean
    public Converter<String, Path> pathConverter() {
        return source -> Paths.get(source);
    }

    @Bean
    public SourceFilter sourceFilter() {
        return p -> true;
    }

    @Bean
    public CoverageService coverageService() {
        return new EmptyCoverageService();
    }

    @Bean
    public JavaParserFacade javaParserFacade() {
        return JavaParserFacade.get(new CombinedTypeSolver(
                new ReflectionTypeSolver()
        ));
    }

    @Bean
    public UnitSource unitSource() {
        return new PathUnitSource(new JavaSourceFinder(cfg), cfg.getSrc(), sourceFilter());
    }
}
