package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
//TODO add dependency mergers
class SpringControllerDelegatingMethodTestGenerator implements TestGenerator {

    private static final String EXPECTED_RESULT = "expectedResult";
    private static final String INSTANCE_NAME = "controllerInstance";
    private static final Set<ImportDeclaration> IMPORT_DECLARATIONS = [
            "import com.fasterxml.jackson.databind.ObjectMapper;",
            "import org.junit.Before;",
            "import org.junit.Test;",
            "import org.mockito.InjectMocks;",
            "import org.mockito.Mock;",
            "import org.mockito.Mockito;",
            "import org.mockito.MockitoAnnotations;",
            "import org.springframework.http.MediaType;",
            "import org.springframework.test.web.servlet.MockMvc;",
            "import org.springframework.test.web.servlet.setup.MockMvcBuilders;",
            "import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;",
            "import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;"].collect {
        JavaParser.parseImport(it)
    }.toSet()

    JavaParserFacade solver
    TestGeneratorResultReporter reporter
    CoverageReporter coverageReporter
    NomenclatureFactory nomenclatures
    ValueFactory valueFactory
    SpringControllerHelper controllerHelper


    SpringControllerDelegatingMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter,
                                                  CoverageReporter coverageReporter, NomenclatureFactory
                                                          nomenclatures, ValueFactory valueFactory, SpringControllerHelper controllerHelper) {
        this.solver = solver
        this.reporter = reporter
        this.coverageReporter = coverageReporter
        this.nomenclatures = nomenclatures
        this.valueFactory = valueFactory
        this.controllerHelper = controllerHelper
    }


    @Override
    Collection<TestGeneratorResult> generate(Unit unit) {
        List<ClassOrInterfaceDeclaration> classes = unit.cu.findAll(ClassOrInterfaceDeclaration).findAll {
            !it.interface && controllerHelper.isRestController(it)
        }
        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unit.javaClass)

        List<TestGeneratorResult> tests = []
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            List<FieldDeclaration> targetFields = classDeclaration.findAll(FieldDeclaration).findAll {
                it.elementType.isClassOrInterfaceType() && !it.static
            }
            if (targetFields.isEmpty()) {
                continue
            }
            TestGeneratorResult classTest = generateClassTests(targetFields, classDeclaration, testMethodNomenclature, unit)
            tests << classTest
        }
        return tests
    }

    private TestGeneratorResult generateClassTests(List<FieldDeclaration> targetFields, ClassOrInterfaceDeclaration classDeclaration, TestMethodNomenclature testMethodNomenclature, Unit unit) {
        Set<FieldDeclaration> testFields = targetFields.collect {
            VariableDeclarator variable = it.variables.first()
            new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                    VariableDeclarator(variable.type, variable.name)).addAnnotation("Mock") //TODO add support
            // for primitive types
        }.toSet()

        FieldDeclaration instanceField = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                VariableDeclarator(new ClassOrInterfaceType(classDeclaration.nameAsString), INSTANCE_NAME))
                .addAnnotation("InjectMocks")

        String setupCode = """
            @Before 
            public void setup(){
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup($INSTANCE_NAME).build();
            }
            """

        MethodDeclaration setupMethod = JavaParser.parseBodyDeclaration(setupCode)
                .asMethodDeclaration()

        FieldDeclaration mockMvcField = JavaParser.parseBodyDeclaration("private MockMvc mockMvc;")

        TestGeneratorResult classTest = new TestGeneratorResult()
        classTest.type = getType()
        for (MethodDeclaration methodDeclaration : classDeclaration.findAll(MethodDeclaration).findAll
                { shouldBeVisited(it) }) {
            TestGeneratorResult methodResult = generateMethodTest(methodDeclaration, classDeclaration, testMethodNomenclature)
            methodResult.tests.forEach {
                it.dependency.fields.addAll(testFields)
                it.dependency.fields << instanceField
                it.dependency.fields << mockMvcField
                it.dependency.methodSetups << setupMethod
                it.dependency.imports.addAll(IMPORT_DECLARATIONS)
                classTest.tests << it
            }
            reporter.publish(methodResult, unit, methodDeclaration)
            coverageReporter.report(unit, methodResult, methodDeclaration)
        }
        classTest
    }

    //TODO add support for spring injected parameters like HttpSession or Pagination
    protected TestGeneratorResult generateMethodTest(MethodDeclaration method, ClassOrInterfaceDeclaration classDeclaration, TestMethodNomenclature testMethodNomenclature) {
        TestGeneratorResult result = new TestGeneratorResult()
        try {
            List<DependableNode<VariableDeclarationExpr>> variables = controllerHelper.getVariableDeclarations(method)
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }

            Statement expectedResulstStatment
            if (method.type && method.type.isClassOrInterfaceType()) {
                expectedResulstStatment = valueFactory.getVariable(EXPECTED_RESULT, method.type).get().node
            }

            Map<String, String> pathVariableToName = controllerHelper.getVariablesMap(method, SpringControllerHelper
                    .PATH_VARIABLE)
            Map<String, String> requestParamToname = controllerHelper.getVariablesMap(method, SpringControllerHelper
                    .REQUEST_PARAM)
            String requestBody = method.parameters.find {
                controllerHelper.hasAnnotation(it, SpringControllerHelper.REQUEST_BODY)
            }?.nameAsString

            AnnotationExpr classRequestMappingAnnotation = controllerHelper.getAnnotation(classDeclaration,
                    SpringControllerHelper
                            .REQUEST_MAPPING_ANNOTATIONS)
            String classUrlTemplate = classRequestMappingAnnotation ? controllerHelper.getUrlTemplate(classRequestMappingAnnotation) : ""
            AnnotationExpr methodRequestMappingAnnotation = controllerHelper.getAnnotation(method,
                    SpringControllerHelper
                            .REQUEST_MAPPING_ANNOTATIONS)
            String methodUrlTemplate = methodRequestMappingAnnotation ? controllerHelper.getUrlTemplate(methodRequestMappingAnnotation) : ""
            String urlTemplate = classUrlTemplate + methodUrlTemplate
            urlTemplate = urlTemplate.isEmpty() ? "/" : urlTemplate
            String url = controllerHelper.fillPathVariablesdUrl(urlTemplate, pathVariableToName)

            String objectMapperCode = requestBody ? "ObjectMapper mapper = new ObjectMapper();" : ""

            MethodCallExpr delegate = method.findAll(MethodCallExpr).last()
            String expectedResulstStatmentCode = ""
            String verifyCode = ""
            if (expectedResulstStatment) {
                expectedResulstStatmentCode = """
                    $expectedResulstStatment
                    Mockito.when(${delegate}).thenReturn(${EXPECTED_RESULT});
                """
            } else {
                String scope = delegate.scope.get().asNameExpr().nameAsString
                String args = delegate.arguments.join(",")
                verifyCode = """
                    Mockito.verify(${scope}).${delegate.nameAsString}($args);
                """
            }
            String sep = System.lineSeparator()
            String contentCode = requestBody ? "${sep}.content(mapper.writeValueAsString" +
                    "($requestBody))" : ""

            String paramsCode = requestParamToname.isEmpty() ? "" : requestParamToname.entrySet().collect {
                "${sep}.param(\"${it.key}\",${it.value}.toString())"
            }
                    .join("")

            String httpMethod = controllerHelper.getHttpMethod(methodRequestMappingAnnotation)

            String expectedJsonCode = getExpectedJsonCode(method, sep)

            String testName = testMethodNomenclature.requestTestMethodName(getType(), method)
            String testCode = """
            @Test
            public void ${testName}() throws Exception {
                ${variableStatements.join(sep)}
                $objectMapperCode
                
                $expectedResulstStatmentCode

                mockMvc.perform($httpMethod("$url")$contentCode$paramsCode
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().is2xxSuccessful())$expectedJsonCode;                       
                
                $verifyCode
             }
            """

            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                    .asMethodDeclaration()
            result.tests = [testMethod]


        } catch (TestGeneratorError tge) {
            result.errors << tge
        }
        return result
    }

    private String getExpectedJsonCode(MethodDeclaration method, String sep) {
        //TODO add support for @JsonAttribute
        List<FieldDeclaration> expectedFields = method.type.findAll(FieldDeclaration)
        String expectedJsonCode = expectedFields.find { !it.static }.collect {
            String fieldName = it.getVariable(0).nameAsString
            String getMethodName = "get${fieldName.charAt(0).toUpperCase()}${fieldName.substring(1)}()"
            """${sep}.andExpect(jsonPath("\$.$fieldName").value($EXPECTED_RESULT.${getMethodName}))"""
        }.join("")
        expectedJsonCode
    }

    TestType getType() {
        return SpringControllersTestTypes.DELEGATING
    }

    boolean shouldBeVisited(MethodDeclaration callableDeclaration) {
        return controllerHelper.isRestControllerMethod(callableDeclaration) &&
                controllerHelper.callDelegateWithParamValuesAndReturnResults(callableDeclaration) &&
                controllerHelper.doNotReassignParameters(callableDeclaration)

    }
}
