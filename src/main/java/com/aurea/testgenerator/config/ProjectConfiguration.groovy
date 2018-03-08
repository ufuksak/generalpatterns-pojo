package com.aurea.testgenerator.config

import groovy.transform.Canonical
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotNull
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
@ConfigurationProperties(prefix = "project")
@EnableConfigurationProperties
@Canonical
@Validated
class ProjectConfiguration {
    boolean blank

    @NotNull
    String src

    String testSrc
    String out
    String jacoco
    String methodPrefix
    boolean disableMethodPrefix
    String resolveJars

    Path getSrcPath() {
        Paths.get(src)
    }

    Path getJacocoPath() {
        Paths.get(jacoco)
    }

    Path getTestSrcPath() {
        Paths.get(testSrc)
    }

    Path getOutPath() {
        Paths.get(out)
    }

    boolean isBlank() {
        return blank
    }

    boolean isDisableMethodPrefix(){
        disableMethodPrefix
    }
}
