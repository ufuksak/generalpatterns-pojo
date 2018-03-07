package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ReturnStmt
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
class SpringControllerHelper {
    protected static final String DEFAULT_ANNOTATION_PROPERTY = "value"
    protected static final String NAME_ANNOTATION_PROPERTY = "name"
    protected static final Set<String> PARAMETER_ANNOTATION_PROPERTIES = [DEFAULT_ANNOTATION_PROPERTY,
                                                                          NAME_ANNOTATION_PROPERTY].toSet()
    static final String PATH_PROPERTY = "path"
    private static final String REQUEST_MAPPING = "RequestMapping"
    private static final String GET_MAPPING = "GetMapping"
    private static final String POST_MAPPING = "PostMapping"
    private static final String PUT_MAPPING = "PutMapping"
    private static final String PATCH_MAPPING = "PatchMapping"
    private static final String DELETE_MAPPING = "DeleteMapping"
    protected static final Set<String> REQUEST_MAPPING_ANNOTATIONS = [REQUEST_MAPPING, GET_MAPPING,
            POST_MAPPING, PUT_MAPPING, PATCH_MAPPING, DELETE_MAPPING].toSet()
    private static final String REST_CONTROLLER = "RestController"
    protected static final String PATH_VARIABLE = "PathVariable"
    protected static final String REQUEST_PARAM = "RequestParam"
    private static final int MAPPING_SUFFIX_LENGTH = 7
    protected static final String REQUEST_BODY = "RequestBody"
    protected static final String DEFAULT_HTTP_METHOD = "get"

    JavaParserFacade solver
    ValueFactory valueFactory

    SpringControllerHelper(JavaParserFacade solver, ValueFactory valueFactory) {
        this.solver = solver
        this.valueFactory = valueFactory
    }

    String getHttpMethod(AnnotationExpr annotationExpr) {
        String annotationName = annotationExpr.nameAsString
        String method
        if (!REQUEST_MAPPING_ANNOTATIONS.contains(annotationName)) {
            throw new IllegalArgumentException("Unsuported annotation type: $annotationName")
        } else if (annotationName == REQUEST_MAPPING) {
            method = getStringValue(annotationExpr, "method").toLowerCase()
        } else {
            method = annotationName.substring(0, annotationName.length()
                    - MAPPING_SUFFIX_LENGTH).toLowerCase()
        }
        method.isEmpty() ? DEFAULT_HTTP_METHOD : method
    }

    String getStringValue(AnnotationExpr annotation, String memberName) {
        if (annotation.isSingleMemberAnnotationExpr() && memberName == DEFAULT_ANNOTATION_PROPERTY) {
            return annotation.asSingleMemberAnnotationExpr().memberValue.asStringLiteralExpr().asString()
        } else if (annotation.isNormalAnnotationExpr()) {
            def pair = annotation.asNormalAnnotationExpr().pairs.find { it.nameAsString == memberName }
            return pair ? pair.value.asStringLiteralExpr().asString() : ""
        } else return ""
    }

    String getStringValue(AnnotationExpr annotation, Set<String> memberNames){
        for(String memberName: memberNames){
            String value = getStringValue(annotation, memberName)
            if(!value.isEmpty()){
                return value
            }
        }
        return ""
    }

    String getStringValue(AnnotationExpr annotation) {
        return getStringValue(annotation, DEFAULT_ANNOTATION_PROPERTY)
    }

    String fillPathVariablesdUrl(String urlTemplate, Map<String, String> pathVariableToName) {
        String url = urlTemplate
        for (String name : pathVariableToName.keySet()) {
            url = url.replaceAll("\\{$name\\}", "\"+${pathVariableToName.get(name)}+\"")
        }
        url
    }

    String getUrlTemplate(AnnotationExpr requestMappingAnnotation) {
        String template = getStringValue(requestMappingAnnotation,
                PATH_PROPERTY)
        template.isEmpty() ? getStringValue(requestMappingAnnotation, DEFAULT_ANNOTATION_PROPERTY) : template
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

    Optional<MethodCallExpr> findLastMethodCallExpression (MethodDeclaration methodDeclaration){
        List<MethodCallExpr> expressions = methodDeclaration.findAll(MethodCallExpr)
        if(!expressions.isEmpty()){
            return Optional.of(expressions.last())
        }
        return Optional.empty()
    }

    boolean callDelegateWithParamValuesAndReturnResults(MethodDeclaration methodDeclaration) {
        List<ReturnStmt> returnStatements = methodDeclaration.findAll(ReturnStmt)
        if (returnStatements.size() > 1) {
            return false
        }
        Optional<MethodCallExpr> optionalExpression = findLastMethodCallExpression(methodDeclaration)
        if (!optionalExpression.isPresent()) {
            return false
        }
        MethodCallExpr methodCallExpr = optionalExpression.get()
        if (!methodCallExpr.scope.isPresent() || !methodCallExpr.scope.get().asNameExpr().resolve().isField()) {
            return false
        }
        if (methodCallExpr.arguments.any { !(it.nameExpr || it.literalExpr) }) {
            return false
        }
        List<String> paramNames = getParamNames(methodDeclaration)
        List<String> usedParameters = methodCallExpr.arguments.findAll { it.nameExpr }.collect {
            it.asNameExpr().nameAsString
        }
        if (!paramNames.containsAll(usedParameters)) {
            return false
        }
        Type returnType = methodDeclaration.type
        if (!returnType || returnType.isVoidType()) {
            return true
        }
        returnType.classOrInterfaceType && methodCallExpr.type == returnType
    }

    List<String> getParamNames(MethodDeclaration methodDeclaration) {
        List<String> paramNames = methodDeclaration.parameters.collect { it.nameAsString }
        paramNames
    }

    boolean isRestControllerMethod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
                hasAnnotation(methodDeclaration, REQUEST_MAPPING_ANNOTATIONS)

    }

    boolean isRestController(ClassOrInterfaceDeclaration classDeclaration) {
        return hasAnnotation(classDeclaration, REST_CONTROLLER)
    }

    boolean hasAnnotation(Node node, Set<String> annotatioNames) {
        node.annotations.any { annotatioNames.contains(it.nameAsString) }
    }

    boolean hasAnnotation(Node node, String annotatioName) {
        hasAnnotation(node, Collections.singleton(annotatioName))
    }

    AnnotationExpr getAnnotation(Node node, Set<String> annotatioNames) {
        return node.annotations.find { annotatioNames.contains(it.nameAsString) }
    }

    AnnotationExpr getAnnotation(Node node, String annotatioName) {
        return getAnnotation(node, Collections.singleton(annotatioName))
    }

    List<DependableNode<VariableDeclarationExpr>> getVariableDeclarations(MethodDeclaration method) {
        List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(method.parameters).map { p ->
            valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                new TestGeneratorError("Failed to build variable for parameter $p of $method")
            }
        }.toList()
        variables
    }

    Map<String, String> getVariablesMap(MethodDeclaration methodDeclaration, String annotationName) {
        methodDeclaration.parameters.collect {
            AnnotationExpr annotation = getAnnotation(it, annotationName)
            if (annotation) {
                Tuple2<String, String> pair = new Tuple2<>(getStringValue(annotation,PARAMETER_ANNOTATION_PROPERTIES)
                        ,  it.nameAsString)
                return Optional.of(pair)
            }
            return Optional.<Tuple2<String, String>> empty()
        }.findAll { it.isPresent() }.collectEntries { [(it.get().first): it.get().second] }
    }
}
