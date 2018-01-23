package com.aurea.testgenerator

import com.aurea.testgenerator.config.PipelineConfiguration
import com.google.common.base.Stopwatch
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import java.util.concurrent.TimeUnit

@Log4j2
@SpringBootApplication
class Main implements CommandLineRunner {

//    @Autowired
//    List<Pipeline> pipelines

    @Autowired
    PipelineConfiguration props

    static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted()
        SpringApplication app = new SpringApplication(Main)
        app.setBannerMode(Banner.Mode.OFF)
        app.run(args)
        log.info("Executed in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms.")
    }

    @Override
    void run(String... args) throws Exception {
        log.info "Props: $props"
//        pipelines.each { it.start() }
//        System.exit(0)
    }
}
