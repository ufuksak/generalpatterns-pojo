package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestDependency
import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.pojo.PojoTestTypes
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.generation.source.PojoFinder
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import static java.lang.System.lineSeparator

@Component
@Profile("open-pojo")
@Log4j2
class OpenPojoTestGenerator implements TestGenerator {

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
            TestGeneratorResult result = new TestGeneratorResult()
            result.type = PojoTestTypes.OPEN_POJO
            if (isPojo(coid)) {
                DependableNode<Statement> validatorStatement = buildValidatorStatement(coid)
                String testName = testMethodNomenclature.requestTestMethodName(result.type, coid)
                String fullTypeName = ASTNodeUtils.getFullTypeName(coid)
                String testText = """
                        @Test
                        public void ${testName}() {
                            ${validatorStatement.node}
                            
                            validator.validate(PojoClassFactory.getPojoClass(${fullTypeName}.class));
                        }
                    """
                try {
                    MethodDeclaration testCode = JavaParser.parseBodyDeclaration(testText).asMethodDeclaration()
                    TestDependency dependency = new TestDependency(
                            imports: [Imports.JUNIT_TEST, Imports.OPEN_POJO_POJO_CLASS_FACTORY]
                    )
                    TestDependency merged = TestNodeMerger.merge(dependency, validatorStatement.dependency)
                    result.tests << DependableNode.from(testCode, merged)
                } catch (ParseProblemException ppe) {
                    log.error "Failed generation of open pojo test!", ppe
                    result.errors << TestGeneratorError.parseFailure(testText)
                }
                tests << result
                reporter.publish(result, unit, coid)
            } else {
                reporter.publish(result, unit, coid)
            }
        }
        tests
    }

    static DependableNode<Statement> buildValidatorStatement(ClassOrInterfaceDeclaration coid) {
        TestDependency dependency = new TestDependency()
        List<String> testers = ['GetterTester']
        dependency.imports = [Imports.OPEN_POJO_VALIDATOR, Imports.OPEN_POJO_GETTER_TESTER,
                              Imports.OPEN_POJO_VALIDATOR, Imports.OPEN_POJO_TEST_CHAIN]
        if (hasAtLeastOneSetter(coid)) {
            testers << 'SetterTester'
            dependency.imports << Imports.OPEN_POJO_SETTER_TESTER
        }
        if (hasToStringMethod(coid)) {
            testers << 'ToStringTester'
            dependency.imports << Imports.OPEN_POJO_TO_STRING_TESTER
        }
        StringBuilder statementCode = new StringBuilder("Validator validator = TestChain")
        statementCode.append(lineSeparator()).append("\t\t").append(".startWith(new ${testers.first()}())")
        for (int i = 1; i < testers.size(); i++) {
            statementCode.append(lineSeparator()).append("\t\t").append(".then(new ${testers[i]}())")
        }
        statementCode.append(lineSeparator()).append("\t\t").append(".buildValidator();")
        Statement statement = JavaParser.parseStatement(statementCode.toString())
        DependableNode.from(statement, dependency)
    }

    static boolean isPojo(ClassOrInterfaceDeclaration coid) {
        hasAtleastOneGetter(coid)
    }

    static boolean hasToStringMethod(ClassOrInterfaceDeclaration coid) {
        coid.methods.any {
            it.nameAsString == 'toString' && it.type.toString() == 'String' && it.public && !it.parameters
        }
    }

    static boolean hasAtLeastOneSetter(ClassOrInterfaceDeclaration coid) {
        for (FieldDeclaration field : coid.fields) {
            try {
                ResolvedFieldDeclaration resolvedField = field.resolve()
                PojoFinder setterFinder = new PojoFinder(resolvedField)
                if (setterFinder.tryToFindSetter().present) {
                    return true
                }
            } catch (UnsolvedSymbolException use) {
                log.debug "Failed to solve $field in $coid", use
            }
        }
        return false
    }

    static boolean hasAtleastOneGetter(ClassOrInterfaceDeclaration coid) {
        for (FieldDeclaration field : coid.fields) {
            try {
                ResolvedFieldDeclaration resolvedField = field.resolve()
                PojoFinder getterFinder = new PojoFinder(resolvedField)
                if (getterFinder.tryToFindGetter().present) {
                    return true
                }
            } catch (UnsolvedSymbolException use) {
                log.debug "Failed to solve $field in $coid", use
            }
        }
        return false
    }
}
