package com.aurea.testgenerator.config

import groovy.transform.Canonical
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotNull
import java.nio.file.Path

@Configuration
@ConfigurationProperties(prefix = "project")
@EnableConfigurationProperties
@Canonical
@Validated
class ProjectConfiguration {
    @NotNull
    Path src

    @NotNull
    Path testSrc

    @NotNull
    Path out

    Path jacoco
}
