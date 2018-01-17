package com.aurea.testgenerator

import com.aurea.testgenerator.config.BaseConfig
import com.aurea.testgenerator.pipeline.Pipeline
import com.google.common.base.Stopwatch
import groovy.util.logging.Log4j2
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.SimpleCommandLinePropertySource

import java.util.concurrent.TimeUnit

@Log4j2
class Main {
    static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted()

        def ctx = new AnnotationConfigApplicationContext()
        def commandLinePropertySource = new SimpleCommandLinePropertySource("args source", args)
        ctx.getEnvironment().getPropertySources().addFirst(commandLinePropertySource)
        ctx.register(BaseConfig)
        ctx.refresh()

        if (commandLinePropertySource.containsProperty('repository')) {
            ctx.getBean('pipeline', Pipeline).start()
        } else {
            ctx.getBean('pipelines', List).forEach { Pipeline p -> p.start() }
        }

        log.info("Generated in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms.")
    }
}
