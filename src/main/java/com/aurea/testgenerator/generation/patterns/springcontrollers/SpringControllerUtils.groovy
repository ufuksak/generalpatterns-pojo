package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.annotations.AnnotationsProcessor
import com.aurea.testgenerator.generation.methods.MethodsUtils
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import org.apache.commons.lang3.StringUtils


class SpringControllerUtils {
    static final String NAME_ANNOTATION_PROPERTY = "name"
    protected
    static final Set<String> PARAMETER_ANNOTATION_PROPERTIES = [AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY,
                                                                NAME_ANNOTATION_PROPERTY]
    protected static final String PATH_PROPERTY = "path"
    protected static final String REQUEST_MAPPING = "RequestMapping"
    static final String GET_MAPPING = "GetMapping"
    static final String POST_MAPPING = "PostMapping"
    static final String PUT_MAPPING = "PutMapping"
    static final String PATCH_MAPPING = "PatchMapping"
    static final String DELETE_MAPPING = "DeleteMapping"
    protected static final Set<String> REQUEST_MAPPING_ANNOTATIONS = [REQUEST_MAPPING, GET_MAPPING,
                                                                      POST_MAPPING, PUT_MAPPING, PATCH_MAPPING, DELETE_MAPPING]
    protected static final String REST_CONTROLLER = "RestController"
    protected static final String PATH_VARIABLE = "PathVariable"
    protected static final String REQUEST_PARAM = "RequestParam"
    protected static final int MAPPING_SUFFIX_LENGTH = 7
    protected static final String REQUEST_BODY = "RequestBody"
    protected static final String DEFAULT_HTTP_METHOD = "get"

    static String getHttpMethod(AnnotationExpr annotationExpr) {
        String annotationName = annotationExpr.nameAsString
        String method
        if (!SpringControllerUtils.REQUEST_MAPPING_ANNOTATIONS.contains(annotationName)) {
            throw new IllegalArgumentException("Unsuported annotation type: $annotationName")
        } else if (annotationName == SpringControllerUtils.REQUEST_MAPPING) {
            method = AnnotationsProcessor.getStringValue(annotationExpr, "method").toLowerCase()
        } else {
            method = annotationName.substring(0, annotationName.length()
                    - SpringControllerUtils.MAPPING_SUFFIX_LENGTH).toLowerCase()
        }
        method = method.isEmpty() ? SpringControllerUtils.DEFAULT_HTTP_METHOD : method
        if (method.contains(".")) {
            method = StringUtils.substringAfterLast(method, ".")
        }
        method
    }

    static String fillPathVariablesUrl(String urlTemplate, Map<String, String> pathVariableToName) {
        String url = urlTemplate
        for (String name : pathVariableToName.keySet()) {
            url = url.replaceAll("\\{$name\\}", "\"+${pathVariableToName.get(name)}+\"")
        }
        url
    }

    static String getUrlTemplate(AnnotationExpr requestMappingAnnotation) {
        String template = AnnotationsProcessor.getStringValue(requestMappingAnnotation,
                SpringControllerUtils.PATH_PROPERTY)
        template.isEmpty() ? AnnotationsProcessor.getStringValue(requestMappingAnnotation, AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY) : template
    }

    static boolean callDelegateWithParamValuesAndReturnResults(MethodDeclaration methodDeclaration) {
        List<ReturnStmt> returnStatements = methodDeclaration.findAll(ReturnStmt)
        if (returnStatements.size() > 1) {
            return false
        }
        Optional<MethodCallExpr> optionalExpression = MethodsUtils.findLastMethodCallExpression(methodDeclaration)
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
        List<String> paramNames = MethodsUtils.getParamNames(methodDeclaration)
        List<String> usedParameters = methodCallExpr.arguments.findAll { it.nameExpr }.collect {
            it.asNameExpr().nameAsString
        }
        if (!paramNames.containsAll(usedParameters)) {
            return false
        }
        return true
    }

    static boolean isRestControllerMethod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
                AnnotationsProcessor.hasAnnotation(methodDeclaration, SpringControllerUtils.REQUEST_MAPPING_ANNOTATIONS)
    }

    static boolean isRestController(ClassOrInterfaceDeclaration classDeclaration) {
        return AnnotationsProcessor.hasAnnotation(classDeclaration, SpringControllerUtils.REST_CONTROLLER)
    }

    static Map<String, String> getVariablesMap(MethodDeclaration methodDeclaration, String annotationName) {
        methodDeclaration.parameters.collect {
            AnnotationExpr annotation = AnnotationsProcessor.getAnnotation(it, annotationName)
            if (annotation) {
                Tuple2<String, String> pair = new Tuple2<>(AnnotationsProcessor.getStringValue(annotation, SpringControllerUtils.PARAMETER_ANNOTATION_PROPERTIES)
                        ,  it.nameAsString)
                return Optional.of(pair)
            }
            return Optional.<Tuple2<String, String>> empty()
        }.findAll { it.isPresent() }.collectEntries { [(it.get().first): it.get().second] }
    }

    static String buildUrl(MethodDeclaration method, AnnotationExpr methodRequestMappingAnnotation) {
        ClassOrInterfaceDeclaration classDeclaration = method.parentNode.get() as ClassOrInterfaceDeclaration
        AnnotationExpr classRequestMappingAnnotation = AnnotationsProcessor.getAnnotation(classDeclaration, SpringControllerUtils.REQUEST_MAPPING_ANNOTATIONS)
        String classUrlTemplate = classRequestMappingAnnotation ? getUrlTemplate(classRequestMappingAnnotation) : ""

        String methodUrlTemplate = methodRequestMappingAnnotation ? getUrlTemplate(methodRequestMappingAnnotation) : ""
        String urlTemplate = classUrlTemplate + methodUrlTemplate
        urlTemplate = urlTemplate.isEmpty() ? "/" : urlTemplate
        Map<String, String> pathVariableToName = getVariablesMap(method, SpringControllerUtils.PATH_VARIABLE)
        String url = fillPathVariablesUrl(urlTemplate, pathVariableToName)
        url
    }
}
