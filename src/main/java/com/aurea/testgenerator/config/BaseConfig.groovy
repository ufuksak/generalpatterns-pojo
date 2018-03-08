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
import org.springframework.beans.factory.annotation.Autowired
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
        CombinedTypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver())

        if (projectConfiguration.resolveJars) {
            projectConfiguration.resolveJars.split(",").each { solver.add(new JarTypeSolver(it)) }
        }
        getJavaParserTypeSolvers(projectConfiguration).each { solver.add(it) }
        solver
    }

    private List<JavaParserTypeSolver> getJavaParserTypeSolvers(ProjectConfiguration cfg) {
        if (!cfg.paths.isEmpty()) {
            cfg.paths.toSet().collect { new JavaParserTypeSolver(cfg.srcPath.resolve(it).toFile()) }
        } else {
            Collections.singletonList(new JavaParserTypeSolver(cfg.srcPath.toFile()))
        }
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
