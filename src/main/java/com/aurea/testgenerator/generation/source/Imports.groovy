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
    static final ImportDeclaration SOFT_ASSERTIONS = parse('org.assertj.core.api.SoftAssertions')
    static final ImportDeclaration COLLECTIONS = parse('java.util.Collections')
    static final ImportDeclaration IMMUTABLE_MAP = parse('com.google.common.collect.ImmutableMap')
    static final ImportDeclaration DATE = parse('java.util.Date')
    static final ImportDeclaration SQL_DATE = parse('java.sql.Date')
    static final ImportDeclaration LOCALE = parse('java.util.Locale')
    static final ImportDeclaration STATIC_MOCK = new ImportDeclaration(new Name('org.mockito.Mockito.mock'), true, false)
    static final ImportDeclaration STATIC_RETURNS_DEEP_STUBS = new ImportDeclaration(new Name('org.mockito.Mockito.RETURNS_DEEP_STUBS'), true, false)

    //OpenPojo
    static final ImportDeclaration OPEN_POJO_VALIDATOR = parse('com.openpojo.validation.Validator')
    static final ImportDeclaration OPEN_POJO_POJO_CLASS_FACTORY = parse('com.openpojo.reflection.impl.PojoClassFactory')
    static final ImportDeclaration OPEN_POJO_GETTER_TESTER = parse('com.openpojo.validation.test.impl.GetterTester')
    static final ImportDeclaration OPEN_POJO_SETTER_TESTER = parse('com.openpojo.validation.test.impl.SetterTester')
    static final ImportDeclaration OPEN_POJO_TEST_CHAIN = parse('com.aurea.unittest.commons.pojo.chain.TestChain')
    static final ImportDeclaration OPEN_POJO_TO_STRING_TESTER = parse('com.aurea.unittest.commons.pojo.ToStringTester')

    //PojoTester
    static final ImportDeclaration POJO_TESTER_ASSERTIONS = new ImportDeclaration(new Name('pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor'), true, false)
    static final ImportDeclaration POJO_TESTER_METHOD = parse('pl.pojo.tester.api.assertion.Method')


    static ImportDeclaration parse(String fullClassName) {
        JavaParser.parseImport("import $fullClassName;")
    }
}
