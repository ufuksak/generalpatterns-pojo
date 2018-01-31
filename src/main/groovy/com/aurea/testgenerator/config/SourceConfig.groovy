package com.aurea.testgenerator.config

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.EmptyCoverageService
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.PathUnitSource
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.UnitSource
import com.github.generator.xml.Converters
import com.github.generator.xml.NodeToXmlConverter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    ProjectConfiguration cfg

    @Bean
    NodeToXmlConverter xmlConverter() {
        Converters.newConverter()
    }

    @Bean
    XPath xPath() {
        XPathFactory.newInstance().newXPath()
    }

    @Bean
    Converter<String, Path> pathConverter() {
        return { Paths.get(it) }
    }

    @Bean
    SourceFilter sourceFilter() {
        return new SourceFilter() {
            @Override
            boolean test(Path path) {
                true
            }
        }
    }

    @Bean
    CoverageService coverageService() {
        new EmptyCoverageService()
    }

    @Bean
    JavaParserFacade javaParserFacade() {
        JavaParserFacade.get(new CombinedTypeSolver(
                new ReflectionTypeSolver()
        ));
    }

    @Bean
    UnitSource unitSource() {
        new PathUnitSource(new JavaSourceFinder(cfg), cfg.getSrc(), sourceFilter())
    }
}
