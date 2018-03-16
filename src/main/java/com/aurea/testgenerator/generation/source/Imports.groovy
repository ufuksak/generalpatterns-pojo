package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Name


class Imports {
    static final ImportDeclaration JUNIT_TEST = parse('org.junit.Test')
    static final ImportDeclaration GENERATED = parse('javax.annotation.Generated')
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
    static final ImportDeclaration OPEN_POJO_TESTERS = parse('com.aurea.unittest.commons.pojo.Testers')
    static final ImportDeclaration OPEN_POJO_GETTER_TESTER = parse('com.openpojo.validation.test.impl.GetterTester')
    static final ImportDeclaration OPEN_POJO_SETTER_TESTER = parse('com.openpojo.validation.test.impl.SetterTester')
    static final ImportDeclaration OPEN_POJO_TO_STRING_TESTER = parse('com.aurea.unittest.commons.pojo.ToStringTester')
    static final ImportDeclaration OPEN_POJO_EQUALS_VERIFIER_TESTER = parse('com.aurea.unittest.commons.pojo.equalsverifier.EqualsVerifierTesterFactory')
    static final ImportDeclaration OPEN_POJO_TEST_CHAIN = parse('com.aurea.unittest.commons.pojo.chain.TestChain')

    //PojoTester
    static final ImportDeclaration POJO_TESTER_ASSERTIONS = parse('pl.pojo.tester.api.assertion.Assertions')
    static final ImportDeclaration POJO_TESTER_ASSERTIONS_POJO_METHODS_FOR = new ImportDeclaration(new Name('pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor'), true, false)
    static final ImportDeclaration POJO_TESTER_METHOD = parse('pl.pojo.tester.api.assertion.Method')

    //Singletons
    static final ImportDeclaration SINGLETON_TESTER = parse('com.aurea.unittest.commons.SingletonTester')
    static final ImportDeclaration CALLABLE = parse('java.util.concurrent.Callable')

    //spring controllers
    static final ImportDeclaration OBJECT_MAPPER = parse('com.fasterxml.jackson.databind.ObjectMapper')
    static final ImportDeclaration JUNIT_BEFORE = parse('org.junit.Before')
    static final ImportDeclaration INJECT_MOCKS = parse('org.mockito.InjectMocks')
    static final ImportDeclaration MOCK = parse('org.mockito.Mock')
    static final ImportDeclaration MOCKITO = parse('org.mockito.Mockito')
    static final ImportDeclaration MOCK_ANNOTATIONS = parse('org.mockito.MockitoAnnotations')
    static final ImportDeclaration MEDIA_TYPE = parse('org.springframework.http.MediaType')
    static final ImportDeclaration MOCK_MVC = parse('org.springframework.test.web.servlet.MockMvc')
    static final ImportDeclaration MOCK_MVC_BUILDERS = parse('org.springframework.test.web.servlet.setup.MockMvcBuilders')
    static final ImportDeclaration STATIC_MOCK_MVC_BUILDERS = new ImportDeclaration(new Name('org.springframework.test.web.servlet.request.MockMvcRequestBuilders'), true, true)
    static final ImportDeclaration STATIC_MOCK_MVC_MATCHERS = new ImportDeclaration(new Name('org.springframework.test.web.servlet.result.MockMvcResultMatchers'), true, true)
    static final ImportDeclaration STATIC_MOCKITO = new ImportDeclaration(new Name('org.mockito.Mockito'), true, true)
    static final List<ImportDeclaration> SPRING_CONTROLLER_IMPORTS = [
            OBJECT_MAPPER,
            JUNIT_BEFORE,
            JUNIT_TEST,
            INJECT_MOCKS,
            MOCK,
            MOCKITO,
            MOCK_ANNOTATIONS,
            MEDIA_TYPE,
            MOCK_MVC,
            MOCK_MVC_BUILDERS,
            STATIC_MOCK_MVC_BUILDERS,
            STATIC_MOCK_MVC_MATCHERS,
            STATIC_MOCKITO]

    static ImportDeclaration parse(String fullClassName) {
        JavaParser.parseImport("import $fullClassName;")
    }
}
