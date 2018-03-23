package com.aurea.testgenerator.generation.patterns.springrepository

import com.aurea.testgenerator.generation.AbstractMethodTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class SpringRepositoryTestGenerator extends AbstractMethodTestGenerator {

    private static final String REPOSITORY = 'Repository'
    private static final String STRING = 'String'
    private static final String LONG = 'Long'
    private static final String TEST_ENTITY_MANAGER = 'TestEntityManager'
    private static final String ENTITY_MANAGER = 'entityManager'
    private static final String AUTOWIRED = 'Autowired'
    private static final String DATA_JPA_TEST = 'DataJpaTest'
    private static final String RUN_WITH = 'RunWith'
    private static final String SPRINGRUNNER = 'SpringRunner'
    private static final String REPOSITORY_LOWER = 'repository'
    private static final String TEST_STRING = '"testString"'
    private static final String SPACE = ' '
    private static final String ASSIGN = '='
    private static final String NEW = 'new'
    private static final String OPEN_BRACE = '('
    private static final String CLOSE_BRACE = ')'
    private static final String SEMICOLON = ';'
    private static final String NEW_LINE = System.getProperty('line.separator')
    private static final String COMMA = ','
    private static final String ENTITY_SET = 'entity.set'

    NomenclatureFactory nomenclatures

    @Autowired
    SpringRepositoryTestGenerator(JavaParserFacade solver,
                                  TestGeneratorResultReporter reporter,
                                  CoverageReporter visitReporter,
                                  NomenclatureFactory nomenclatures) {
        super(solver, reporter, visitReporter, nomenclatures)
        this.nomenclatures = nomenclatures
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()

        DependableNode<MethodDeclaration> testMethod = new DependableNode<>()

        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
        String testName = testMethodNomenclature
                .requestTestMethodName(SpringRepositoryTestTypes.SPRING_REPOSITORY_FIND_ENTITY, method)
        ClassOrInterfaceDeclaration parentClass = method.getAncestorOfType(ClassOrInterfaceDeclaration).get()

        Optional<String> classNameOptional = parentClass.extendedTypes[0].childNodes.stream()
                .map { it.toString() }.filter() {
            !(it.contains(REPOSITORY) || it.contains(STRING) || it.contains(LONG))
        }
        .findFirst()
        classNameOptional.ifPresent {
            fillTestMethod(it, testName, method, testMethod, result)
        }

        result
    }

    private fillTestMethod(String className, String testName, MethodDeclaration method,
                           DependableNode<MethodDeclaration> testMethod, result) {
        String methodName = method.nameAsString
        def firstParamName = method.parameters[0].name.asString().capitalize()
        String testCode = """
                @Test
                public void ${testName}() throws Exception {
                    // given
                    ${className} entity = new ${className}();

                    ${fillParams(method)}
                    entityManager.persist(entity);
                    entityManager.flush();
                    
                    // when
                    ${className} found = repository.${methodName}(${listParams(method)});
                    
                    // then
                    assertThat(found.get${firstParamName}()).isEqualTo(entity.get${firstParamName}());
                }
            """

        testMethod.node = JavaParser.parseBodyDeclaration(testCode).asMethodDeclaration()
        fillClassLevel(testMethod, method)
        result.tests << testMethod
    }

    def listParams(MethodDeclaration methodDeclaration) {
        def sb = new StringBuilder()
        boolean first = true
        methodDeclaration.parameters.each {
            if (!first) {
                sb.append(COMMA).append(SPACE)
            } else {
                first = false
            }
            sb.append(it.name.asString())
        }
        sb.toString()
    }

    // TODO: parse param types, assign default values
    // TODO: parse collections types
    // TODO: parse other standard method names
    def fillParams(MethodDeclaration methodDeclaration) {
        def sb = new StringBuilder()
        methodDeclaration.parameters.each {
            def fieldName = it.name.asString()
            def fieldType = it.type.asString()
            sb.append(fieldType).append(SPACE).append(fieldName).append(SPACE).append(ASSIGN)
                    .append(SPACE).append(NEW).append(SPACE).append(fieldType).append(OPEN_BRACE).append(TEST_STRING)
                    .append(CLOSE_BRACE).append(SEMICOLON).append(NEW_LINE)
            sb.append(ENTITY_SET).append(fieldName.capitalize()).append(OPEN_BRACE).append(fieldName)
                    .append(CLOSE_BRACE).append(SEMICOLON).append(NEW_LINE)
        }
        sb.toString()
    }

    private void fillClassLevel(DependableNode<MethodDeclaration> testMethod, MethodDeclaration method) {
        fillClassAnnotations(testMethod)
        fillFields(method, testMethod)
        fillImports(testMethod)
    }

    private void fillImports(DependableNode<MethodDeclaration> testMethod) {
        testMethod.dependency.imports << Imports.JUNIT_TEST
        testMethod.dependency.imports << Imports.JUNIT_RUNWITH
        testMethod.dependency.imports << Imports.SPRING_AUTOWIRED
        testMethod.dependency.imports << Imports.SPRING_TESTENTITYMANAGER
        testMethod.dependency.imports << Imports.SPRING_DATAJPATEST
        testMethod.dependency.imports << Imports.SPRING_SPRINGRUNNER
        testMethod.dependency.imports << Imports.ASSERTJ_ASSERTTHAT
    }

    private void fillClassAnnotations(DependableNode<MethodDeclaration> testMethod) {
        testMethod.dependency.classAnnotations << new SingleMemberAnnotationExpr(new Name(RUN_WITH),
                new ClassExpr(JavaParser.parseClassOrInterfaceType(SPRINGRUNNER)))
        testMethod.dependency.classAnnotations << new MarkerAnnotationExpr(DATA_JPA_TEST)
    }

    private void fillFields(MethodDeclaration method, DependableNode<MethodDeclaration> testMethod) {
        def testEntityManagerField = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE),
                new VariableDeclarator(JavaParser.parseClassOrInterfaceType(TEST_ENTITY_MANAGER), ENTITY_MANAGER))
                .addAnnotation(AUTOWIRED)
        testMethod.dependency.fields << testEntityManagerField
        ClassOrInterfaceDeclaration parentClass = method.getAncestorOfType(ClassOrInterfaceDeclaration).get()
        def repositoryField = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE),
                new VariableDeclarator(JavaParser.parseClassOrInterfaceType(parentClass.name.identifier),
                        REPOSITORY_LOWER)).addAnnotation(AUTOWIRED)
        testMethod.dependency.fields << repositoryField
    }

    @Override
    protected TestType getType() {
        SpringRepositoryTestTypes.SPRING_REPOSITORY_FIND_ENTITY
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, MethodDeclaration method) {
        super.shouldBeVisited(unit, method) && isInterfaceExtendedFromRepository(method)
    }

    static boolean isInterfaceExtendedFromRepository(MethodDeclaration method) {
        def parentNode = method.parentNode.get()
        if (parentNode instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration coid = parentNode
            coid.isInterface && coid.extendedTypes.stream().map { it.name.identifier }.anyMatch {
                it.contains REPOSITORY
            }
        } else false
    }
}
