package com.aurea.testgenerator.config

import spock.lang.Specification

import java.util.stream.Collectors


class MavenDependencyResolverSpec extends Specification {

    def "should fail when given invalid path to pom"() {
        when:
        MavenDependencyResolver resolver = new MavenDependencyResolver("invalid path")
        resolver.jars()

        then:
        def e = thrown(MavenReaderException)
        e.cause == null
    }

    def "should fail when given path to invalid pom"() {
        when:
        onPom("invalid-pom.xml")

        then:
        thrown(MavenReaderException)
    }

    def "should find jars in simple pom"() {
        when:
        List<String> jars = onPom("single-dependency-pom.xml")

        then:
        !jars.isEmpty()
        jars.get(0).contains('junit')
    }

    def "should find parent dependency"() {
        when:
        List<String> jars = onPom("single-dependency-pom.xml")

        then:
        !jars.isEmpty()
        jars.get(0).contains('junit')
    }

    private List<String> onPom(String name) {
        URL url = this.class.getResource(name)
        new MavenDependencyResolver(url.file).jars().collect(Collectors.toList())
    }
}
