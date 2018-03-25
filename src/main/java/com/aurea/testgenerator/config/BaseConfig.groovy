package com.aurea.testgenerator.config

import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.JacocoCoverageRepository
import com.aurea.testgenerator.coverage.JacocoCoverageService
import com.aurea.testgenerator.coverage.NoCoverageService
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.source.SourceFilters
import com.github.javaparser.JavaParser
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

import javax.annotation.PostConstruct

@Configuration
@EnableAspectJAutoProxy
class BaseConfig {

    @PostConstruct
    void disableCommentNodes() {
        JavaParser.getStaticConfiguration().setStoreTokens(false)
        JavaParser.getStaticConfiguration().setAttributeComments(false)
    }

    @Bean
    StopWatchAspect stopWatchAspect() {
        new StopWatchAspect()
    }

    @Bean
    SourceFilter sourceFilter() {
        SourceFilters.empty()
    }

    @Bean
    TypeSolver combinedTypeSolver(ProjectConfiguration projectConfiguration) {
        CombinedTypeSolver solver = new CombinedTypeSolver(new ReflectionTypeSolver())

        solver.add(new JavaParserTypeSolver(new File(projectConfiguration.src)))

        projectConfiguration.resolvePaths.stream()
                .map { new File(it) }
                .filter { it.exists() && it.isDirectory() }
                .forEach {
                    solver.add(new JavaParserTypeSolver(it))
                }

        projectConfiguration.resolveJars.stream()
                .map { new File(it) }
                .filter { it.exists() }
                .filter { (it.isFile() && it.name.endsWith(".jar")) || it.isDirectory() }
                .forEach {
                    if (it.isDirectory()) {
                        it.traverse {
                            if (it.isFile() && it.name.endsWith(".jar")) {
                                solver.add(new JarTypeSolver(it.path))
                            }
                        }
                    } else if (it.name.endsWith(".jar")){
                        solver.add(new JarTypeSolver(it.path))
                    }
                }

        solver
    }


    @Bean
    JavaParserFacade javaParserFacade(TypeSolver solver) {
        JavaParserFacade.get(solver)
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
