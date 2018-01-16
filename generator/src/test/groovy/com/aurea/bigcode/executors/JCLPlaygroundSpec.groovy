package com.aurea.bigcode.executors

import org.xeustechnologies.jcl.JarClassLoader
import org.xeustechnologies.jcl.JclObjectFactory
import spock.lang.Specification

class JCLPlaygroundSpec extends Specification {

    def "Should be able to load a class"() {
        setup:
        JarClassLoader jcl = new JarClassLoader()
        jcl.add("D:/crossover/repos/Sandbox/build/libs/Sandbox.jar")
        JclObjectFactory factory = JclObjectFactory.getInstance()

        when:
        Object obj = factory.create(jcl, "com.aurea.sample.Foo")

        then:
        println obj.getClass().getDeclaredMethods()
    }


}
