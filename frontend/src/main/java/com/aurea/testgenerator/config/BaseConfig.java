package com.aurea.testgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableAspectJAutoProxy
public class BaseConfig {
    @Bean
    public StopWatchAspect stopWatchAspect() {
        return new StopWatchAspect();
    }
}
