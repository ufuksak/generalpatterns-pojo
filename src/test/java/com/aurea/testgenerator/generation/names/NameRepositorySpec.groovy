package com.aurea.testgenerator.generation.names

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import spock.lang.Specification


class NameRepositorySpec extends Specification {

    NameRepository nameRepository = new NameRepository()


    def "no arg constructor"() {

        List<ConstructorDeclaration> constructos

    }

    List<ConstructorDeclaration> withCode(String code) {
        JavaParser.parse(code).findAll(ConstructorDeclaration)
    }
}
