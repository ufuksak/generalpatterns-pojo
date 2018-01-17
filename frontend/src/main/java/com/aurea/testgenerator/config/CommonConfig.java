package com.aurea.testgenerator.config;

import com.aurea.testgenerator.pattern.MatcherRepository;
import com.aurea.testgenerator.pattern.MatcherRepositoryImpl;
import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
public class CommonConfig {

    @Bean
    public MatcherRepository matcherRepository() {
        return new MatcherRepositoryImpl();
    }

    @Bean
    public NodeToXmlConverter xmlConverter() {
        return Converters.newConverter();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }
}
