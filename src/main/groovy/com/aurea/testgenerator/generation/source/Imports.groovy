package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Name

class Imports {
    static final ImportDeclaration JUNIT_TEST = parse('org.junit.Test')
    static final ImportDeclaration EXCEPTION = parse('java.lang.Exception')
    static final ImportDeclaration JUNIT_RUNWITH = parse('org.junit.runner.RunWith')
    static final ImportDeclaration JUNITPARAMS_JUNITPARAMSRUNNER = parse('junitparams.JUnitParamsRunner')
    static final ImportDeclaration JUNITPARAMS_PARAMETERS = parse('junitparams.Parameters')
    static final ImportDeclaration ASSERTJ_OFFSET = parse('org.assertj.core.data.Offset')
    static final ImportDeclaration ASSERTJ_ASSERTTHAT = new ImportDeclaration(new Name('org.assertj.core.api.Assertions.assertThat'), true, false)

    static ImportDeclaration parse(String fullClassName) {
        JavaParser.parseImport("import $fullClassName;")
    }
}
