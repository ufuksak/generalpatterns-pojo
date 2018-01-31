package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Name

class Imports {
    static final ImportDeclaration JUNIT_TEST = JavaParser.parseImport('org.junit.Test')
    static final ImportDeclaration JUNIT_RUNWITH = JavaParser.parseImport('org.junit.runner.RunWith')
    static final ImportDeclaration JUNITPARAMS_JUNITPARAMSRUNNER = JavaParser.parseImport('junitparams.JUnitParamsRunner')
    static final ImportDeclaration JUNITPARAMS_PARAMETERS = JavaParser.parseImport('junitparams.Parameters')
    static final ImportDeclaration ASSERTJ_OFFSET = JavaParser.parseImport('org.assertj.core.data.Offset')
    static final ImportDeclaration ASSERTJ_ASSERTTHAT = new ImportDeclaration(new Name('org.assertj.core.api.Assertions.assertThat'), true, false)
}
