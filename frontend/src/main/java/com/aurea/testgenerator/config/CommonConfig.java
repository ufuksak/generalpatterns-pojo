package com.aurea.testgenerator.config;

import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
public class CommonConfig {

    @Value("${name}")
    private String name;

    @PostConstruct
    public void s() {
        System.out.println("NAME: " + name);
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
