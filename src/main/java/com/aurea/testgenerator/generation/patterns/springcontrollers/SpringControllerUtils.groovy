package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.annotations.AnnotationsProcessor
import com.aurea.testgenerator.generation.methods.MethodsUtils
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.jasongoodwin.monads.Try
import groovy.util.logging.Log4j2
import org.apache.commons.lang3.StringUtils

import java.util.stream.Collectors

@Log4j2
class SpringControllerUtils {
    static final String NAME_ANNOTATION_PROPERTY = "name"
    static final Set<String> PARAMETER_ANNOTATION_PROPERTIES = [AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY,
                                                                NAME_ANNOTATION_PROPERTY]
    static final String PATH_PROPERTY = "path"
    static final String REST_CONTROLLER = "RestController"
    static final String PATH_VARIABLE = "PathVariable"
    static final String REQUEST_PARAM = "RequestParam"
    static final String REQUEST_HEADER = "RequestHeader"
    static final String REQUEST_BODY = "RequestBody"
    static final String DEFAULT_HTTP_METHOD = "get"

    static String getHttpMethod(AnnotationExpr annotationExpr) {
        String method = RequestMappingAnnotation.of(annotationExpr.nameAsString).method
        method = method ?: AnnotationsProcessor.getStringValue(annotationExpr, "method").toLowerCase()
        method = method.contains(".") ? StringUtils.substringAfterLast(method, ".") : method
        method ?: DEFAULT_HTTP_METHOD
    }

    private static String fillPathVariablesUrl(String urlTemplate, Map<String, String> pathVariableToName) {
        String url = urlTemplate
        pathVariableToName.each { key, value ->
            url = url.replaceAll("\\{$key\\}", "\"+$value+\"")
        }

        url
    }

    private static String getUrlTemplate(AnnotationExpr requestMappingAnnotation) {
        String template = AnnotationsProcessor.getStringValue(requestMappingAnnotation, PATH_PROPERTY)
        template ?: AnnotationsProcessor.getStringValue(requestMappingAnnotation, AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY)
    }

    static boolean callDelegateWithParamValuesAndReturnResults(MethodDeclaration methodDeclaration) {
        if (methodDeclaration.findAll(ReturnStmt).size() > 1) {
            return false
        }
        Optional<MethodCallExpr> optionalExpression = MethodsUtils.findLastMethodCallExpression(methodDeclaration)
        if (!optionalExpression.isPresent()) {
            return false
        }
        MethodCallExpr methodCallExpr = optionalExpression.get()
        if (!callsDelegate(methodCallExpr)) {
            return false
        }
        if (!returnSameType(methodDeclaration, methodCallExpr)) {
            return false
        }
        if (methodCallExpr.arguments.any { !(it.nameExpr || it.literalExpr) }) {
            return false
        }
        List<String> paramNames = MethodsUtils.getParamNames(methodDeclaration)
        List<String> usedParameters = methodCallExpr.arguments.findAll { it.nameExpr }*.asNameExpr().nameAsString
        if (!paramNames.containsAll(usedParameters)) {
            return false
        }
        return true
    }

    private static boolean returnSameType(MethodDeclaration methodDeclaration, MethodCallExpr methodCallExpr) {
        if (methodDeclaration.type.isVoidType()){
            return true
        }
        Try.ofFailable { methodDeclaration.type.resolve() == methodCallExpr.calculateResolvedType() }
                .orElse(false)
    }

    private static boolean callsDelegate(MethodCallExpr methodCallExpr) {
        Try.ofFailable {
            methodCallExpr.scope.map { it.isNameExpr() && it.asNameExpr().resolve().isField() }.orElse(false)
        }.onFailure {
            log.error("Unresoolvable return type for method $methodCallExpr", it)
        }.orElse(false)
    }

    static boolean isRestControllerMethod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
                AnnotationsProcessor.hasAnnotation(methodDeclaration, RequestMappingAnnotation.names())
    }

    static boolean isRestController(ClassOrInterfaceDeclaration classDeclaration) {
        return AnnotationsProcessor.hasAnnotation(classDeclaration, REST_CONTROLLER)
    }

    static Map<String, String> getAnnotatedVariablesMap(MethodDeclaration methodDeclaration, String annotationName) {
        methodDeclaration.parameters.findAll {
            AnnotationsProcessor.getAnnotation(it, annotationName)
        }.collectEntries {
            def annotation = AnnotationsProcessor.getAnnotation(it, annotationName)
            def key = AnnotationsProcessor.getStringValue(annotation, PARAMETER_ANNOTATION_PROPERTIES) ?: it.nameAsString
            [key, it.nameAsString]
        }
    }

    static String buildUrl(MethodDeclaration method) {
        ClassOrInterfaceDeclaration classDeclaration = method.getAncestorOfType(ClassOrInterfaceDeclaration).get()
        String classUrlTemplate = getNodeUrlTemplate(classDeclaration)
        String methodUrlTemplate = getNodeUrlTemplate(method)
        String urlTemplate = classUrlTemplate + methodUrlTemplate
        String urlSep = "/"
        urlTemplate = urlTemplate ?: urlSep
        urlTemplate = urlTemplate.startsWith(urlSep) ? urlTemplate : (urlSep +  urlTemplate)
        Map<String, String> pathVariableToNames = getAnnotatedVariablesMap(method, PATH_VARIABLE)
        fillPathVariablesUrl(urlTemplate, pathVariableToNames)
    }

    private static String getNodeUrlTemplate(NodeWithAnnotations node) {
        AnnotationExpr mappingAnnotation = AnnotationsProcessor.getAnnotation(node, RequestMappingAnnotation.names())
        mappingAnnotation ? getUrlTemplate(mappingAnnotation) : ""
    }

    static boolean hasSimpleUrlTemplate(NodeWithAnnotations node) {
        AnnotationExpr mappingAnnotation = AnnotationsProcessor.getAnnotation(node, RequestMappingAnnotation.names())
        if(!mappingAnnotation){
            return true
        }
        AnnotationsProcessor.getAnnotationMemberExpressionValue(mappingAnnotation,
                [AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY, PATH_PROPERTY])
                .map{it.isStringLiteralExpr()}.orElse(true)
    }
}
