package com.aurea.testgenerator.config

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.EmptyCoverageService
import com.aurea.testgenerator.source.*
import com.github.generator.xml.Converters
import com.github.generator.xml.NodeToXmlConverter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
class SourceConfig {

    @Bean
    NodeToXmlConverter xmlConverter() {
        Converters.newConverter()
    }

    @Bean
    XPath xPath() {
        XPathFactory.newInstance().newXPath()
    }

    @Bean
    @ConfigurationPropertiesBinding
    Converter<String, Path> pathConverter() {
        new Converter<String, Path>() {
            @Override
            Path convert(String source) {
                Paths.get(source)
            }
        }
    }

    @Bean
    SourceFilter sourceFilter() {
        SourceFilters.empty()
    }

    @Bean
    CoverageService coverageService() {
        new EmptyCoverageService()
    }

    @Bean
    JavaParserFacade javaParserFacade() {
        JavaParserFacade.get(new CombinedTypeSolver(
                new ReflectionTypeSolver()
        ))
    }

    @Bean
    UnitSource unitSource(ProjectConfiguration cfg) {
        new PathUnitSource(new JavaSourceFinder(cfg), cfg.getSrc(), sourceFilter())
    }
}
