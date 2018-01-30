package com.aurea.testgenerator.config;

import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ComponentScan(value = "com.aurea.testgenerator")
public class CommonConfig {

    @Bean
    public NodeToXmlConverter xmlConverter() {
        return Converters.newConverter();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

    @Bean
    public Converter<String, Path> pathConverter() {
        return source -> Paths.get(source);
    }
}
