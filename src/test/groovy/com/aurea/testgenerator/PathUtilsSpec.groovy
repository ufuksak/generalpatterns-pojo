package com.aurea.testgenerator


import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.expr.Name
import spock.lang.Specification

import static java.nio.file.Paths.get

class PathUtilsSpec extends Specification {

    def "package name to path converts it properly"() {
        expect:
        PathUtils.packageNameToPath(input) == expectation

        where:
        input         | expectation
        ""            | get("")
        "example"     | get("example")
        "org.example" | get("org", "example")
    }

    def "package declaration to path converts it properly"() {
        expect:
        PathUtils.packageToPath(input) == expectation

        where:
        input                 | expectation
        ofName("example")     | get("example")
        ofName("org.example") | get("org", "example")
    }

    private static PackageDeclaration ofName(String name) {
        new PackageDeclaration(new Name(name))
    }
}
