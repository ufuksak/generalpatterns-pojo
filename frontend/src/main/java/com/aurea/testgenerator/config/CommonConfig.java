package com.aurea.testgenerator.config;

import com.aurea.testgenerator.coverage.CoverageService;
import com.aurea.testgenerator.coverage.EmptyCoverageService;
import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
public class CommonConfig {

    @Bean
    public CoverageService noCoverage() {
        return new EmptyCoverageService();
    }

    @Bean
    public JavaParserFacade javaParserFacade() {
        return JavaParserFacade.get(new CombinedTypeSolver(
                new ReflectionTypeSolver()
        ));
    }

    @Bean
    public NodeToXmlConverter xmlConverter() {
        return Converters.newConverter();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }
}
