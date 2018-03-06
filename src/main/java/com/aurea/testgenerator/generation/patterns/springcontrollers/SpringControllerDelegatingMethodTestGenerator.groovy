package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.google.common.collect.Sets
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
//TODO add dependency mergers
class SpringControllerDelegatingMethodTestGenerator implements TestGenerator {
    private static final String DEFAULT_ANNOTATION_PROPERTY = "value"
    static final String PATH_PROPERTY = "path"
    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"
    private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"
    private static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
    private static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
    private static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping"
    private static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
    private static final Set<String> REQUEST_MAPPING_ANNOTATIONS = Sets.newHashSet(REQUEST_MAPPING, GET_MAPPING,
            POST_MAPPING, PUT_MAPPING, PATCH_MAPPING, DELETE_MAPPING).asImmutable()

    private static final String EXPECTED_RESULT = "expectedResult";
    private static final String INSTANCE_NAME = "controllerInstance";
    private static final int MAPPING_SUFFIX_LENGTH = 6;
    private static final Set<ImportDeclaration> IMPORT_DECLARATIONS = [
            "import com.devfactory.easycover.coverage.data.dto.CoverageTO;",
            "import com.devfactory.easycover.coverage.service.ProjectInfoService;",
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
        JavaParser
                .parseImport(it)
    }.toSet()

    JavaParserFacade solver
    TestGeneratorResultReporter reporter
    CoverageReporter coverageReporter
    NomenclatureFactory nomenclatures
    ValueFactory valueFactory


    SpringControllerDelegatingMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter,
                                                  CoverageReporter coverageReporter, NomenclatureFactory
                                                          nomenclatures, ValueFactory valueFactory) {
        this.solver = solver
        this.reporter = reporter
        this.coverageReporter = coverageReporter
        this.nomenclatures = nomenclatures
        this.valueFactory =  valueFactory
    }


    @Override
    Collection<TestGeneratorResult> generate(Unit unit) {
        List<ClassOrInterfaceDeclaration> classes = unit.cu.findAll(ClassOrInterfaceDeclaration).findAll {
            !it.interface && isRestController(it)
        }
        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unit.javaClass)

        List<TestGeneratorResult> tests = []
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            List<FieldDeclaration> targetFields = classDeclaration.findAll(FieldDeclaration)
            if (targetFields.isEmpty()) {
                continue
            }
            Set<FieldDeclaration> testFields = targetFields.collect {
                VariableDeclarator variable = it.variables.first()
                new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                        VariableDeclarator(variable.type, variable.name)).addAnnotation("Mock") //TODO add support
                // for primitive types
            }.toSet()

            FieldDeclaration instanceField = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                    VariableDeclarator(new ClassOrInterfaceType(classDeclaration.name), INSTANCE_NAME))
                    .addAnnotation("InjectMocks")

            String setupCode = """
            @Before 
            public void setup(){
                mockMvc = MockMvcBuilders.standaloneSetup(coverageController).build();
            }
            """

            MethodDeclaration setupMethod = JavaParser.parseBodyDeclaration(setupCode)
                    .asMethodDeclaration()

            TestGeneratorResult classTest = new TestGeneratorResult()
            classTest.type = getType()
            for (MethodDeclaration methodDeclaration : classDeclaration.findAll(MethodDeclaration).findAll
                    {shouldBeVisited(it)}) {
                TestGeneratorResult result = generate(methodDeclaration, classDeclaration, testMethodNomenclature)
                result.tests.forEach {
                    it.dependency.fields << testFields
                    it.dependency.fields << instanceField
                    it.dependency.methodSetups << setupMethod
                    it.dependency.imports << IMPORT_DECLARATIONS
                    reporter.publish(it, unit, methodDeclaration)
                    coverageReporter.report(unit, it, methodDeclaration)
                    classTest.tests << it
                }
            }
            tests << classTest
        }
        return tests
    }

    //TODO add support for spring injected parameters like HttpSession or Pagination
    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, ClassOrInterfaceDeclaration classDeclaration, TestMethodNomenclature testMethodNomenclature) {
        TestGeneratorResult result = new TestGeneratorResult()
        try {
            List<DependableNode<VariableDeclarationExpr>> variables = getVariableDeclarations(method)
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }

            MethodCallExpr delegate = method.findAll(MethodCallExpr).first()
            Statement delegateCallStatment = new ExpressionStmt(delegate.clone())
            Statement expectedResulstStatment
            if (method.type) {
                expectedResulstStatment = valueFactory.getVariable(EXPECTED_RESULT, method.type).get().node
            }

            Map<String, String> pathVariableToName = getVariablesMap(method, "org.springframework.web.bind.annotation" +
                    ".PathVariable", DEFAULT_ANNOTATION_PROPERTY)
            Map<String, String> requestParamToname = getVariablesMap(method, "org.springframework.web.bind.annotation" +
                    ".RequestParam", DEFAULT_ANNOTATION_PROPERTY)
            String requestBody = method.parameters.find {
                hasAnnotation(it, "org.springframework.web.bind.annotation" +
                        ".RequestBody")
            }?.nameAsString

            AnnotationExpr classRequestMappingAnnotation = getAnnotation(classDeclaration, REQUEST_MAPPING_ANNOTATIONS)
            String classUrlTemplate = getUrlTemplate(classRequestMappingAnnotation)
            AnnotationExpr methodRequestMappingAnnotation = getAnnotation(method, REQUEST_MAPPING_ANNOTATIONS)
            String urlTemplate = classUrlTemplate + getUrlTemplate(methodRequestMappingAnnotation)
            String url = fillPathVariablesdUrl(urlTemplate, pathVariableToName)

            String testName = testMethodNomenclature.requestTestMethodName(getType(), method)
            String objectMapperCode = requestBody ? "ObjectMapper mapper = new ObjectMapper();" : ""

            String expectedResulstStatmentCode = ""
            String verifyCode = ""
            if (expectedResulstStatment) {
                expectedResulstStatmentCode = """
                    $expectedResulstStatment
                    Mockito.when(${delegateCallStatment}).thenReturn(${EXPECTED_RESULT});
                """
            } else {
                verifyCode = """
                    Mockito.verify(${delegateCallStatment});
                """
            }
            String sep = System.lineSeparator()
            String contentCode = requestBody ? "${sep}.content(mapper.writeValueAsString" +
                    "($requestBody))" : ""

            String paramsCode = requestParamToname.isEmpty() ? "" : "${sep}" + requestParamToname
                    .collect { "${sep}.param(${it.key},${it.value}.toString())" }

            String httpMethod = getHttpMethod(methodRequestMappingAnnotation)
            //TODO add support for @JsonAttribute
            List<FieldDeclaration> expectedFields = method.type.findAll(FieldDeclaration)
            String expectedJsonCode = expectedFields.find {!it.static}.collect {
                String fieldName = it.getVariable(0).nameAsString
                String getMethodName = "get${fieldName.charAt(0).toUpperCase()}${fieldName.substring(1)}()"
                return """${sep}.andExpect(jsonPath("\$.$fieldName").value($EXPECTED_RESULT.${getMethodName}))"""
            }
            String testCode = """
            @Test
            public void ${testName}() throws Exception {
                MockitoAnnotations.initMocks(this);
                ${variableStatements.join(System.lineSeparator())}
                $objectMapperCode
                
                $expectedResulstStatmentCode
                
                MockMvc mockMvc = MockMvcBuilders.standaloneSetup($INSTANCE_NAME).build();
                mockMvc.perform($httpMethod("$url")$contentCode$paramsCode
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().is2xxSuccessful())$expectedJsonCode;                       
                
                $verifyCode
             }
            """

            DependableNode<MethodDeclaration> testMethod = new DependableNode<>()
            testMethod.node = JavaParser.parseBodyDeclaration(testCode)
                    .asMethodDeclaration()
            testMethod.dependency.imports << Imports.JUNIT_TEST
            result.tests = [testMethod]


        } catch (TestGeneratorError tge) {
            result.errors << tge
        }
        return result
    }

    String getHttpMethod(AnnotationExpr annotationExpr) {
        String annotationName = getQualifiedName(annotationExpr)
        if (!REQUEST_MAPPING_ANNOTATIONS.contains(annotationName)) {
            throw new IllegalArgumentException("Unsuported annotation type: $annotationName")
        } else if (annotationName == REQUEST_MAPPING) {
            return getStringValue(annotationName, "method").toLowerCase()
        } else {
            return annotationName.substring(annotationName.lastIndexOf("."), annotationName.length()
                    - MAPPING_SUFFIX_LENGTH).toLowerCase()
        }
    }

    private String fillPathVariablesdUrl(String urlTemplate, Map<String, String> pathVariableToName) {
        String url = urlTemplate
        for (String name : pathVariableToName.keySet()) {
            url = url.replaceAll("{$name}", "\"+${pathVariableToName.get(name)}+\"")
        }
        if (url.endsWith("+\"")) {
            url = url.substring(0, url.length() - 2)
        }
        url
    }

    private String getUrlTemplate(AnnotationExpr requestMappingAnnotation) {
        requestMappingAnnotation ? getStringValue(requestMappingAnnotation,
                PATH_PROPERTY) : ""
    }

    Map<String, String> getVariablesMap(MethodDeclaration methodDeclaration, String annotationName, String memberName) {
        methodDeclaration.parameters.collect {
            AnnotationExpr annotation = getAnnotation(it, annotationName)
            if (annotation) {
                Tuple2<String, String> pair = new Tuple2<>(getStringValue(annotation), it.nameAsString, memberName)
                return Optional.of(pair)
            }
            return Optional.<Tuple2<String, String>> empty()
        }.findAll { it.isPresent() }.collectEntries { [(it.get().first): it.get().second] }
    }

    String getStringValue(AnnotationExpr annotation, String memberName) {
        if (annotation.isSingleMemberAnnotationExpr()) {
            return annotation.asSingleMemberAnnotationExpr().memberValue.asStringLiteralExpr().asString()
        } else if (annotation.isNormalAnnotationExpr()) {
            def pair = annotation.asNormalAnnotationExpr().pairs.find { it.nameAsString == memberName }
            return pair ? pair.value.asStringLiteralExpr().asString() : ""
        } else return ""
    }

    @Override
    protected TestType getType() {
        return SpringControllersTestTypes.DELEGATING
    }

    protected boolean shouldBeVisited(MethodDeclaration callableDeclaration) {
        return  isRestControllerMehod(callableDeclaration) &&
                callDelegateWithParamValuesAndReturnResults(callableDeclaration) &&
                doNotReassignParameters(callableDeclaration)

    }

    boolean doNotReassignParameters(MethodDeclaration methodDeclaration) {
        List<String> paramNames = getParamNames(methodDeclaration)
        if (paramNames.isEmpty()) {
            return true
        }
        List<AssignExpr> assignExprs = methodDeclaration.findAll(AssignExpr)
        List<String> assignedNames = assignExprs.collect { it.target.asNameExpr().nameAsString }
        return assignedNames.disjoint(paramNames)
    }

    private boolean callDelegateWithParamValuesAndReturnResults(MethodDeclaration methodDeclaration) {
        List<ReturnStmt> returnStatements = method.findAll(ReturnStmt).findAll { it.expression.present }
        if (returnStatements.size() > 1) {
            return false
        }
        def returnStatement = returnStatements.first();
        Expression expression = returnStatement.expression.get()
        if (!expression instanceof MethodCallExpr) {
            return false
        }
        MethodCallExpr methodCallExpr = expression as MethodCallExpr
        if (!methodCallExpr.scope.isPresent() || !methodCallExpr.scope.get().asNameExpr().resolve().isField()) {
            return false
        }
        if (methodCallExpr.arguments.any { !it.nameExpr || !it.literalExpr }) {
            return false
        }
        List<String> paramNames = getParamNames(methodDeclaration)
        List<String> usedParameters = methodCallExpr.arguments.findAll { it.nameExpr }.collect {
            it.asNameExpr().nameAsString
        }
        if (!paramNames.containsAll(usedParameters)) {
            return false
        }
        Type returnType = method.type
        if (!returnType) {
            return true
        }
        returnType.classOrInterfaceType && methodCallExpr.type == returnType
    }

    private List<String> getParamNames(MethodDeclaration methodDeclaration) {
        List<String> paramNames = methodDeclaration.parameters.collect { it.name }
        paramNames
    }

    private boolean isRestControllerMehod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
                hasAnnotation(methodDeclaration, REQUEST_MAPPING_ANNOTATIONS)

    }

    boolean isRestController(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration = node.get() as ClassOrInterfaceDeclaration
        return hasAnnotation(classDeclaration, "org.springframework.web.bind.annotation.RestController")
    }

    boolean hasAnnotation(BodyDeclaration bodyDeclaration, Set<String> annotatioNames) {
        bodyDeclaration.annotations.any { annotatioNames.contains(getQualifiedName(it)) }
    }

    boolean hasAnnotation(BodyDeclaration bodyDeclaration, String annotatioName) {
        hasAnnotation(bodyDeclaration, Collections.singleton(annotatioName))
    }

    String getQualifiedName(AnnotationExpr annotationExpr) {
        solver.getType(annotationExpr).asReferenceType().qualifiedName
    }

    String getAnnotation(BodyDeclaration bodyDeclaration, Set<String> annotatioNames) {
        return bodyDeclaration.annotations.find { annotatioNames.contains(getQualifiedName(it)) }
    }

    String getAnnotation(BodyDeclaration bodyDeclaration, String annotatioName) {
        return getAnnotation(bodyDeclaration, Collections.singleton(annotatioName))
    }

    protected List<DependableNode<VariableDeclarationExpr>> getVariableDeclarations(MethodDeclaration method) {
        List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(method.parameters).map { p ->
            valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                new TestGeneratorError("Failed to build variable for parameter $p of $method")
            }
        }.toList()
        variables
    }
}
