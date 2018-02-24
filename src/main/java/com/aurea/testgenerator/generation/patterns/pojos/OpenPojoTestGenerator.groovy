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
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.reporting.CoverageReporter
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
        for (ClassOrInterfaceDeclaration coid : classes) {
            TestGeneratorResult result = new TestGeneratorResult()
            result.type = PojoTestTypes.OPEN_POJO
            if (Callability.isInstantiable(coid) && Pojos.isPojo(coid)) {
                if (Pojos.hasAtleastOneGetter(coid)) {
                    publishAndAddResolved(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'getterTester',
                                    PojoTestTypes.OPEN_POJO_GETTER),
                            tests,
                            unit,
                            Pojos.getGetters(coid))
                }
                if (Pojos.hasAtLeastOneSetter(coid)) {
                    publishAndAddResolved(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'setterTester',
                                    PojoTestTypes.OPEN_POJO_SETTER),
                            tests,
                            unit,
                            Pojos.getSetters(coid))
                }
                Pojos.tryGetToStringMethod(coid).map { toStringMethod ->
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'toStringTester',
                                    PojoTestTypes.OPEN_POJO_TO_STRING),
                            tests,
                            unit,
                            toStringMethod)
                }
                Optional<MethodDeclaration> maybeEqualsMethod = Pojos.tryGetEqualsMethod(coid)
                Optional<MethodDeclaration> maybeHashCodeMethod = Pojos.tryGetHashCodeMethod(coid)
                if (maybeEqualsMethod.present || maybeHashCodeMethod.present) {
                    List<MethodDeclaration> methods = new ArrayList<>(2)
                    maybeEqualsMethod.ifPresent { methods << it }
                    maybeHashCodeMethod.ifPresent { methods << it }
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'equalsHashCodeTester',
                                    PojoTestTypes.OPEN_POJO_EQUALS),
                            tests,
                            unit,
                            methods)
                }
                List<ConstructorDeclaration> constructors = coid.constructors
                if (constructors) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'constructorTester',
                                    PojoTestTypes.OPEN_POJO_CONSTRUCTORS),
                            tests,
                            unit,
                            constructors)
                }
            } else {
                reporter.publish(new TestGeneratorResult(type: PojoTestTypes.OPEN_POJO), unit, [])
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

    private static TestGeneratorResult buildTest(ClassOrInterfaceDeclaration coid,
                                                 TestMethodNomenclature testMethodNomenclature,
                                                 String testerName,
                                                 TestType type) {
        String fullTypeName = ASTNodeUtils.getFullTypeName(coid)
        TestGeneratorResult result = new TestGeneratorResult()
        result.type = type
        String testName = testMethodNomenclature.requestTestMethodName(result.type, coid)
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
