package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.ast.TestDependency
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO
import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO_CONSTRUCTORS
import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO_EQUALS
import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO_GETTER
import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO_SETTER
import static com.aurea.testgenerator.generation.patterns.pojos.PojoTestTypes.OPEN_POJO_TO_STRING

@Component
@Profile("open-pojo")
@Log4j2
class OpenPojoTestGenerator implements TestGenerator {

    @Autowired
    TestGeneratorResultReporter reporter

    @Autowired
    CoverageReporter coverageReporter

    @Autowired
    NomenclatureFactory nomenclatures

    @Override
    Collection<TestGeneratorResult> generate(Unit unit) {
        List<ClassOrInterfaceDeclaration> classes = unit.cu.findAll(ClassOrInterfaceDeclaration).findAll {
            !it.interface
        }
        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unit.javaClass)

        List<TestGeneratorResult> tests = []
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            TestGeneratorResult result = new TestGeneratorResult()
            result.type = OPEN_POJO
            if (Callability.isInstantiable(classDeclaration) && Pojos.isPojo(classDeclaration)) {
                if (Pojos.hasAtleastOneGetter(classDeclaration)) {
                    def test = buildTest(classDeclaration, testMethodNomenclature, 'getterTester', OPEN_POJO_GETTER)
                    publishAndAddResolved(test, tests, unit, Pojos.getGetters(classDeclaration))
                }

                if (Pojos.hasAtLeastOneSetter(classDeclaration)) {
                    def test = buildTest(classDeclaration, testMethodNomenclature, 'setterTester', OPEN_POJO_SETTER)
                    publishAndAddResolved(test, tests, unit, Pojos.getSetters(classDeclaration))
                }

                Pojos.tryGetToStringMethod(classDeclaration).ifPresent { toStringMethod ->
                    def test = buildTest(classDeclaration, testMethodNomenclature, 'toStringTester', OPEN_POJO_TO_STRING)
                    publishAndAdd(test, tests, unit, toStringMethod)
                }

                Optional<MethodDeclaration> maybeEqualsMethod = Pojos.tryGetEqualsMethod(classDeclaration)
                Optional<MethodDeclaration> maybeHashCodeMethod = Pojos.tryGetHashCodeMethod(classDeclaration)
                if (maybeEqualsMethod.present || maybeHashCodeMethod.present) {
                    List<MethodDeclaration> methods = new ArrayList<>(2)
                    maybeEqualsMethod.ifPresent { methods << it }
                    maybeHashCodeMethod.ifPresent { methods << it }

                    def test = buildTest(classDeclaration, testMethodNomenclature, 'equalsHashCodeTester', OPEN_POJO_EQUALS)
                    publishAndAdd(test, tests, unit, methods)
                }

                List<ConstructorDeclaration> constructors = classDeclaration.constructors
                if (constructors) {
                    def test = buildTest(classDeclaration, testMethodNomenclature, 'constructorTester', OPEN_POJO_CONSTRUCTORS)
                    publishAndAdd(test, tests, unit, constructors)
                }
            } else {
                reporter.publish(new TestGeneratorResult(type: OPEN_POJO), unit, [])
            }
        }
        tests
    }

    private void publishAndAdd(TestGeneratorResult testGeneratorResult,
                               List<TestGeneratorResult> results,
                               Unit unit,
                               MethodDeclaration testedMethod) {
        publishAndAdd(testGeneratorResult, results, unit, Collections.singletonList(testedMethod))
    }

    private void publishAndAddResolved(TestGeneratorResult testGeneratorResult,
                                       List<TestGeneratorResult> results,
                                       Unit unit,
                                       List<ResolvedMethodDeclaration> testedMethods) {
        reporter.publishResolved(testGeneratorResult, unit, testedMethods)
        coverageReporter.reportResolved(unit, testGeneratorResult, testedMethods)
        results << testGeneratorResult
    }

    private void publishAndAdd(TestGeneratorResult testGeneratorResult,
                               List<TestGeneratorResult> results,
                               Unit unit,
                               List<CallableDeclaration> testedMethods) {
        reporter.publish(testGeneratorResult, unit, testedMethods)
        coverageReporter.report(unit, testGeneratorResult, testedMethods)
        results << testGeneratorResult
    }

    private static TestGeneratorResult buildTest(ClassOrInterfaceDeclaration classDeclaration,
                                                 TestMethodNomenclature testMethodNomenclature,
                                                 String testerName,
                                                 TestType type) {
        String fullTypeName = ASTNodeUtils.getFullTypeName(classDeclaration)
        TestGeneratorResult result = new TestGeneratorResult()
        result.type = type
        String testName = testMethodNomenclature.requestTestMethodName(result.type, classDeclaration)
        String testText = """
            @Test
            public void ${testName}() {
                Validator validator = TestChain.startWith(Testers.${testerName}()).buildValidator();
                
                validator.validate(PojoClassFactory.getPojoClass(${fullTypeName}.class));
            }"""
        try {
            MethodDeclaration testCode = JavaParser.parseBodyDeclaration(testText).asMethodDeclaration()
            result.tests << DependableNode.from(testCode, new TestDependency(
                    imports: [Imports.JUNIT_TEST,
                              Imports.OPEN_POJO_TEST_CHAIN,
                              Imports.OPEN_POJO_VALIDATOR,
                              Imports.OPEN_POJO_POJO_CLASS_FACTORY,
                              Imports.OPEN_POJO_TESTERS,
                    ]
            ))
        } catch (ParseProblemException ppe) {
            log.error "Failed generation of pojo tester test!", ppe
            result.errors << TestGeneratorError.parseFailure(testText)
        }
        result
    }
}
