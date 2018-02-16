package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestDependency
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.pojo.PojoTestTypes
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.Statement
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("pojo-tester")
@Log4j2
class PojoTesterGenerator implements TestGenerator {

    @Autowired
    TestGeneratorResultReporter reporter

    @Autowired
    VisitReporter visitReporter

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
            if (Pojos.isPojo(coid)) {
                if (Pojos.hasAtleastOneGetter(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'GETTER',
                                    PojoTestTypes.POJO_TESTER_GETTER),
                            tests,
                            unit,
                            coid)
                }
                if (Pojos.hasAtLeastOneSetter(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'SETTER',
                                    PojoTestTypes.POJO_TESTER_SETTER),
                            tests,
                            unit,
                            coid)
                }
                if (Pojos.hasToStringMethod(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'TO_STRING',
                                    PojoTestTypes.POJO_TESTER_TO_STRING),
                            tests,
                            unit,
                            coid)
                }
                if (Pojos.hasEquals(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'EQUALS',
                                    PojoTestTypes.POJO_TESTER_EQUALS),
                            tests,
                            unit,
                            coid)

                }
                if (Pojos.hasHashCode(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'HASH_CODE',
                                    PojoTestTypes.POJO_TESTER_HASH_CODE),
                            tests,
                            unit,
                            coid)

                }
                if (Pojos.hasConstructors(coid)) {
                    publishAndAdd(
                            buildTest(coid,
                                    testMethodNomenclature,
                                    'CONSTRUCTOR',
                                    PojoTestTypes.POJO_TESTER_CONSTRUCTORS),
                            tests,
                            unit,
                            coid)

                }
            } else {
                reporter.publish(new TestGeneratorResult(type: PojoTestTypes.POJO_TESTER), unit, coid)
            }
        }
        tests
    }

    private void publishAndAdd(TestGeneratorResult testGeneratorResult,
                               List<TestGeneratorResult> results,
                               Unit unit,
                               ClassOrInterfaceDeclaration coid) {
        reporter.publish(testGeneratorResult, unit, coid)
        results << testGeneratorResult
    }

    private static TestGeneratorResult buildTest(ClassOrInterfaceDeclaration coid,
                                                 TestMethodNomenclature testMethodNomenclature,
                                                 String methodType,
                                                 TestType type) {
        String fullTypeName = ASTNodeUtils.getFullTypeName(coid)
        TestGeneratorResult result = new TestGeneratorResult()
        result.type = type
        Statement assertStmt = JavaParser.parseStatement("""
            Assertions.assertPojoMethodsFor(${fullTypeName}.class)
                      .testing(Method.${methodType})
                      .areWellImplemented();    
        """)
        DependableNode<Statement> assertionStatement = DependableNode.from(assertStmt, new TestDependency(
                imports: [Imports.POJO_TESTER_METHOD, Imports.POJO_TESTER_ASSERTIONS, Imports.POJO_TESTER_ASSERTIONS_POJO_METHODS_FOR]
        ))
        String testName = testMethodNomenclature.requestTestMethodName(result.type, coid)
        String testText = """
                        @Test
                        public void ${testName}() {
                            ${assertionStatement.node}
                        }
                    """
        try {
            MethodDeclaration testCode = JavaParser.parseBodyDeclaration(testText).asMethodDeclaration()
            assertionStatement.dependency.imports << Imports.JUNIT_TEST
            result.tests << DependableNode.from(testCode, assertionStatement.dependency)
        } catch (ParseProblemException ppe) {
            log.error "Failed generation of pojo tester test!", ppe
            result.errors << TestGeneratorError.parseFailure(testText)
        }
        result
    }
}
