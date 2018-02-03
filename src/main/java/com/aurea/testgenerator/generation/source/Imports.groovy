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
    static final ImportDeclaration COLLECTIONS = parse('java.util.Collections')
    static final ImportDeclaration IMMUTABLE_MAP = parse('com.google.common.collect.ImmutableMap')
    static final ImportDeclaration DATE = parse('java.util.Date')
    static final ImportDeclaration SQL_DATE = parse('java.sql.Date')
    static final ImportDeclaration STATIC_MOCK = new ImportDeclaration(new Name('org.mockito.Mockito.mock'), true, false)

    static ImportDeclaration parse(String fullClassName) {
        JavaParser.parseImport("import $fullClassName;")
    }
}
