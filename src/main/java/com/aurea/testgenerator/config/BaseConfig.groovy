package com.aurea.testgenerator.config

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
}
