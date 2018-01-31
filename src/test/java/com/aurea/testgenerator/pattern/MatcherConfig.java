package com.aurea.testgenerator.pattern;

import com.github.generator.xml.Converters;
import com.github.generator.xml.NodeToXmlConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Configuration
public class MatcherConfig {

    @Bean
    public NodeToXmlConverter xmlConverter() {
        return Converters.newConverter();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

}
