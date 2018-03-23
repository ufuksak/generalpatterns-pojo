package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.MethodLevelTestGeneratorWithClassContext
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.annotations.AnnotationsProcessor
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.methods.MethodsUtils
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.Types
import com.aurea.testgenerator.value.VariableFactoryReplacingMocksByNewInstances
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
//TODO add dependency mergers
class SpringControllerDelegatingMethodTestGenerator extends MethodLevelTestGeneratorWithClassContext<MethodDeclaration> {

    public static final String MOCK_ANNOTATION = "Mock"
    public static final String INJECT_MOCK_ANNOTATION = "InjectMocks"

    private static final String EXPECTED_RESULT = "expectedResult"
    private static final String INSTANCE_NAME = "controllerInstance"
    private static final String SETUP_CODE = """
            @Before 
            public void setup(){
                MockitoAnnotations.initMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup($INSTANCE_NAME).build();
            }
            """
    private static final MethodDeclaration SETUP_METHOD = JavaParser.parseBodyDeclaration(SETUP_CODE)
            .asMethodDeclaration()
    private static final FieldDeclaration MOCKMVC_FIELD = JavaParser.parseBodyDeclaration("private MockMvc mockMvc;")
            .asFieldDeclaration()

    VariableFactoryReplacingMocksByNewInstances variableFactory


    SpringControllerDelegatingMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter,
                                                  CoverageReporter coverageReporter, NomenclatureFactory nomenclatures,
                                                  VariableFactoryReplacingMocksByNewInstances variableFactory) {
        super(solver, reporter, coverageReporter, nomenclatures)
        this.variableFactory = variableFactory
    }

    private static List<FieldDeclaration> getTargetFields(ClassOrInterfaceDeclaration classDeclaration) {
        classDeclaration.findAll(FieldDeclaration).findAll {
            it.elementType.isClassOrInterfaceType() && !it.static
        }
    }

    @Override
    protected void visitClass(ClassOrInterfaceDeclaration classDeclaration, List<TestGeneratorResult> results) {
        Set<FieldDeclaration> mockedDelegates = createDelegateMocks(classDeclaration)
        FieldDeclaration controllerInstance = createControllerInstance(classDeclaration)

        for(TestGeneratorResult methodResult: results){
            methodResult.tests.forEach {
                it.dependency.fields.addAll(mockedDelegates)
                it.dependency.fields << controllerInstance
                it.dependency.fields << MOCKMVC_FIELD.clone()
                it.dependency.methodSetups << SETUP_METHOD.clone()
                it.dependency.imports.addAll(Imports.SPRING_CONTROLLER_IMPORTS)
            }
        }
    }

    private FieldDeclaration createControllerInstance(ClassOrInterfaceDeclaration classDeclaration){
        new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                VariableDeclarator(new ClassOrInterfaceType(classDeclaration.nameAsString), INSTANCE_NAME))
                .addAnnotation(INJECT_MOCK_ANNOTATION)
    }

    private Set<FieldDeclaration> createDelegateMocks(ClassOrInterfaceDeclaration classDeclaration){
        List<FieldDeclaration> targetFields = getTargetFields(classDeclaration)
        targetFields.collect {
            it.clone().addAnnotation(MOCK_ANNOTATION).setModifiers(EnumSet.of(Modifier.PRIVATE))
        }.toSet()
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration method, Unit unit) {
        TestGeneratorResult result = new TestGeneratorResult()
        try {
            MethodCallExpr delegateExpression = method.findAll(MethodCallExpr).last()
            String requestBodyName = getRequestBodyName(method)
            AnnotationExpr methodRequestMappingAnnotation = AnnotationsProcessor.getAnnotation(method,
                    RequestMappingAnnotation.names())

            DependableNode<MethodDeclaration> testMethod = buildTestMethod(unit, method, delegateExpression,
                    requestBodyName, methodRequestMappingAnnotation)

            result.tests = [testMethod]

        } catch (TestGeneratorError tge) {
            result.errors << tge
        }

        result
    }

    private String getRequestBodyName(MethodDeclaration method){
        method.parameters.find {
            AnnotationsProcessor.hasAnnotation(it, SpringControllerUtils.REQUEST_BODY)
        }?.nameAsString
    }

    private DependableNode<MethodDeclaration> buildTestMethod(Unit unit, MethodDeclaration method, MethodCallExpr delegateExpression,
                                                              String requestBodyName,
                                                              AnnotationExpr methodRequestMappingAnnotation) {
        String testName = getTestMethodName(unit, method)
        String args = getArgs(method, delegateExpression)

        String testCode = """
            @Test
            public void ${testName}() throws Exception {
                ${getVariableStatements(method)}
                ${getObjectMapperCode(requestBodyName)}
                
                ${getExpectedResultStatementCode(method, delegateExpression, args)}
                
                mockMvc.perform(${SpringControllerUtils.getHttpMethod(methodRequestMappingAnnotation)}("${SpringControllerUtils.buildUrl(method)}")
                ${getContentCode(requestBodyName)}\
                ${getContentTypeCode(requestBodyName, methodRequestMappingAnnotation)}\
                ${getParamsCode(method)}\
                ${getHeadersCode(method)}\
                ${getAcceptCode(methodRequestMappingAnnotation)})
                .andExpect(status().is2xxSuccessful());                       
                
                ${getVerifyCode(delegateExpression, args)}
             }
            """
       return getTestMethod(method, testCode)
    }

    private String getTestMethodName(Unit unit, MethodDeclaration method){
        TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unit.javaClass)
        testMethodNomenclature.requestTestMethodName(getType(), method)
    }

    private static String getScope(MethodCallExpr delegateExpression) {
        delegateExpression.scope.get().asNameExpr().nameAsString
    }

    private String getExpectedResultStatementCode(MethodDeclaration method, MethodCallExpr delegateExpression, String args) {
        getExpectedResultDepNode(method).map{
            """
            $it.node;
            Mockito.when(${getScope(delegateExpression)}.${delegateExpression.nameAsString}($args)).thenReturn(${EXPECTED_RESULT});
            """ }.orElse("")
    }

    private Optional<DependableNode<VariableDeclarationExpr>> getExpectedResultDepNode(MethodDeclaration method) {
        if (MethodsUtils.returnsClassOrInterface(method)) {
            Optional.of(variableFactory.getVariable(EXPECTED_RESULT, method.type))
        } else {
            Optional.empty()
        }
    }

    private static String getContentCode(String requestBodyName) {
        requestBodyName ? "${System.lineSeparator()}.content(mapper.writeValueAsString($requestBodyName))" : ""
    }

    private static GString getVerifyCode(MethodCallExpr delegateExpression, String args) {
        """Mockito.verify(${getScope(delegateExpression)}).${delegateExpression.nameAsString}($args);"""
    }

    private static String getObjectMapperCode(String requestBodyName) {
        requestBodyName ? "ObjectMapper mapper = new ObjectMapper();" : ""
    }

    private static String getParamsCode(MethodDeclaration method) {
        getParametersCode(method, "param", SpringControllerUtils.REQUEST_PARAM)
    }

    private static String getHeadersCode(MethodDeclaration method) {
        getParametersCode(method, "header", SpringControllerUtils
                .REQUEST_HEADER)
    }

    private static String getParametersCode(MethodDeclaration method, String parameterMethod,  String annotationName){
        SpringControllerUtils.getAnnotatedVariablesMap(method, annotationName).collect {
                "${System.lineSeparator()}.${parameterMethod}(\"${it.key}\", String.valueOf(${it.value}))"
            }.join("")
    }

    private String getVariableStatements(MethodDeclaration method) {
        List<DependableNode<VariableDeclarationExpr>> variables = variableFactory.getVariableDeclarations(method)
        variables.collect { new ExpressionStmt(it.node) }.join(System.lineSeparator())
    }

    private static String getArgs(MethodDeclaration method, MethodCallExpr delegateExpression) {
        Map<String, Type> methodParamToType = method.parameters.collectEntries { [(it.nameAsString), it.type] }
        String args = delegateExpression.arguments.collect {
            Type argType = methodParamToType.get(it.toString())
            if (argType && argType.isClassOrInterfaceType()
                    && !Types.isString(argType)
                    && !Types.isBoxedPrimitive(argType)) {
                "any(${argType.name.asString()}.class)"
            } else {
                "eq(${it})"
            }
        }.join(",")
        args
    }

    private DependableNode<MethodDeclaration> getTestMethod(MethodDeclaration methodDeclaration, String testCode) {
        DependableNode<MethodDeclaration> testMethod = new DependableNode<>()

        getExpectedResultDepNode(methodDeclaration)
                .ifPresent{TestNodeMerger.appendDependencies(testMethod, it)}

        testMethod.node = JavaParser.parseBodyDeclaration(testCode).asMethodDeclaration()
        testMethod
    }

    private static String getContentTypeCode(String requestBodyName, AnnotationExpr methodRequestMappingAnnotation) {
        AnnotationsProcessor.getAnnotationMemberExpressionValue(methodRequestMappingAnnotation, "consumes")
                .map{ ".contentType(${getChildNodeIfArray(it)})" }
                .orElse(requestBodyName ? '.contentType("application/json;charset=UTF-8")' : "")

    }

    private static Expression getChildNodeIfArray(Expression expression){
        if(expression.isArrayInitializerExpr() && ! expression.childNodes.isEmpty()){
            return expression.findAll(Expression).first()
        }
        return expression
    }

    private static String getAcceptCode(AnnotationExpr methodRequestMappingAnnotation) {
        AnnotationsProcessor.getAnnotationMemberExpressionValue(methodRequestMappingAnnotation, "produces")
                .map{ """.accept(MediaType.parseMediaType(${getChildNodeIfArray(it)}))""" }
                .orElse("")
    }

    @Override
    TestType getType() {
        return SpringControllersTestTypes.DELEGATING
    }

    @Override
    boolean shouldBeVisited(Unit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        !classOrInterfaceDeclaration.interface &&
                SpringControllerUtils.isRestController(classOrInterfaceDeclaration) &&
                SpringControllerUtils.hasSimpleUrlTemplate(classOrInterfaceDeclaration) &&
                !getTargetFields(classOrInterfaceDeclaration).isEmpty()
    }

    @Override
    boolean shouldBeVisited(Unit unit, MethodDeclaration methodDeclaration) {
        return SpringControllerUtils.isRestControllerMethod(methodDeclaration) &&
                SpringControllerUtils.callDelegateWithParamValuesAndReturnResults(methodDeclaration) &&
                SpringControllerUtils.hasSimpleUrlTemplate(methodDeclaration) &&
                MethodsUtils.doNotReassignParameters(methodDeclaration)
    }
}
