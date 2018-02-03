package com.aurea.testgenerator;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(
        exclude = {
                JmxAutoConfiguration.class,
                GroovyTemplateAutoConfiguration.class,
                JacksonAutoConfiguration.class,
                ProjectInfoAutoConfiguration.class,
                WebClientAutoConfiguration.class
        }
)
public class Main implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(Main.class.getSimpleName());

    @Autowired
    Pipeline pipeline;

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        SpringApplication app = new SpringApplication(Main.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        logger.info("Executed in {}", stopwatch);
    }

    @Override
    public void run(String... args) throws Exception {
        pipeline.start();
    }
}
