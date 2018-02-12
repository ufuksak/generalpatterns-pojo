package com.aurea.testgenerator.config

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.JacocoCoverageRepository
import com.aurea.testgenerator.coverage.JacocoCoverageService
import com.aurea.testgenerator.coverage.NoCoverageService
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.SourceFilters
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
class BaseConfig {
    @Bean
    StopWatchAspect stopWatchAspect() {
        new StopWatchAspect()
    }

    @Bean
    SourceFilter sourceFilter() {
        SourceFilters.empty()
    }                                                        

    @Bean
    JavaParserFacade javaParserFacade(ProjectConfiguration projectConfiguration) {
        JavaParserFacade.get(new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(projectConfiguration.srcPath.toFile())
        ))
    }

    @Bean
    CoverageService coverageService(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration.jacoco) {
            return new JacocoCoverageService(JacocoCoverageRepository.fromFile(projectConfiguration.jacocoPath))
        } else {
            return new NoCoverageService()
        }
    }
}
