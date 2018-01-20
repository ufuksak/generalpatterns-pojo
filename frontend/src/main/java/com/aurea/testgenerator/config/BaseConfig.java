package com.aurea.testgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class BaseConfig {
    @Bean
    public StopWatchAspect stopWatchAspect() {
        return new StopWatchAspect();
    }
}
