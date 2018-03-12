package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.generation.annotations.AnnotationsProcessor
import com.aurea.testgenerator.generation.methods.MethodsUtils
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import groovy.util.logging.Log4j2
import org.apache.commons.lang3.StringUtils

@Log4j2
class SpringControllerUtils {
    static final String NAME_ANNOTATION_PROPERTY = "name"
    static final Set<String> PARAMETER_ANNOTATION_PROPERTIES = [AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY,
                                                                NAME_ANNOTATION_PROPERTY]
    static final String PATH_PROPERTY = "path"
    static final String REQUEST_MAPPING = "RequestMapping"
    static final String GET_MAPPING = "GetMapping"
    static final String POST_MAPPING = "PostMapping"
    static final String PUT_MAPPING = "PutMapping"
    static final String PATCH_MAPPING = "PatchMapping"
    static final String DELETE_MAPPING = "DeleteMapping"
    static final Set<String> REQUEST_MAPPING_ANNOTATIONS = [REQUEST_MAPPING, GET_MAPPING,
                                                                      POST_MAPPING, PUT_MAPPING, PATCH_MAPPING,
                                                            DELETE_MAPPING].toSet()
    static final String REST_CONTROLLER = "RestController"
    static final String PATH_VARIABLE = "PathVariable"
    static final String REQUEST_PARAM = "RequestParam"
    static final String REQUEST_HEADER = "RequestHeader"
    static final int MAPPING_SUFFIX_LENGTH = 7
    static final String REQUEST_BODY = "RequestBody"
    static final String DEFAULT_HTTP_METHOD = "get"

    static String getHttpMethod(AnnotationExpr annotationExpr) {
        String annotationName = annotationExpr.nameAsString
        String method
        if (!REQUEST_MAPPING_ANNOTATIONS.contains(annotationName)) {
            throw new IllegalArgumentException("Unsuported annotation type: $annotationName")
        } else if (annotationName == REQUEST_MAPPING) {
            method = AnnotationsProcessor.getStringValue(annotationExpr, "method").toLowerCase()
        } else {
            method = annotationName.substring(0, annotationName.length()
                    - MAPPING_SUFFIX_LENGTH).toLowerCase()
        }
        method = method.isEmpty() ? DEFAULT_HTTP_METHOD : method
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
                PATH_PROPERTY)
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
        if(!callsDelegate(methodCallExpr)){
            return false
        }
        if(!returnSameType(methodDeclaration,methodCallExpr)){
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

    static boolean returnSameType(MethodDeclaration methodDeclaration, MethodCallExpr methodCallExpr) {
        try {
            if (methodDeclaration.type.isVoidType()){
                return true
            }
            return methodDeclaration.type.resolve() == methodCallExpr.calculateResolvedType()
        } catch (Exception ex) { // catching Exception here because com.github.javaparser.symbolsolver
            // .javaparsermodel.JavaParserFacade.solveMethodAsUsage thros RuntimeExdeption
            log.error("Unable to resolve types for $methodDeclaration and $methodCallExpr", ex)
            return false
        }
    }

    static boolean callsDelegate(MethodCallExpr methodCallExpr) {
        if (!methodCallExpr.scope.isPresent()) {
            return false
        }
        try{
            Expression scope = methodCallExpr.scope.get()
            if(!scope.isNameExpr()){
                return false
            }
            ResolvedValueDeclaration resolvedScope = scope.asNameExpr().resolve()
            if(!resolvedScope.isField()){
                return false
            }
        }catch (UnsolvedSymbolException ex){
            log.error("Unresoolvable return type for method $methodCallExpr", ex)
            return false
        }
        return true
    }

    static boolean isRestControllerMethod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
                AnnotationsProcessor.hasAnnotation(methodDeclaration, REQUEST_MAPPING_ANNOTATIONS)
    }

    static boolean isRestController(ClassOrInterfaceDeclaration classDeclaration) {
        return AnnotationsProcessor.hasAnnotation(classDeclaration, REST_CONTROLLER)
    }

    static Map<String, String> getVariablesMap(MethodDeclaration methodDeclaration, String annotationName) {
        methodDeclaration.parameters.collect {
            AnnotationExpr annotation = AnnotationsProcessor.getAnnotation(it, annotationName)
            if (annotation) {
                String key = AnnotationsProcessor.getStringValue(annotation, PARAMETER_ANNOTATION_PROPERTIES)
                Tuple2<String, String> pair = new Tuple2<>(key.isEmpty()?it.nameAsString:key
                        ,  it.nameAsString)
                return Optional.of(pair)
            }
            return Optional.<Tuple2<String, String>> empty()
        }.findAll { it.isPresent() }.collectEntries { [(it.get().first): it.get().second] }
    }

    static String buildUrl(MethodDeclaration method) {
        ClassOrInterfaceDeclaration classDeclaration = method.parentNode.get() as ClassOrInterfaceDeclaration
        String classUrlTemplate = getNodeUrlTemplate(classDeclaration)

        String methodUrlTemplate = getNodeUrlTemplate(method)
        String urlTemplate = classUrlTemplate + methodUrlTemplate
        String urlSep = "/"
        urlTemplate = urlTemplate.isEmpty() ? urlSep : urlTemplate
        urlTemplate = urlTemplate.startsWith(urlSep) ? urlTemplate : (urlSep +  urlTemplate)
        Map<String, String> pathVariableToName = getVariablesMap(method, PATH_VARIABLE)
        String url = fillPathVariablesUrl(urlTemplate, pathVariableToName)
        url
    }

    private static String getNodeUrlTemplate(Node node) {
        AnnotationExpr mappingAnnotation = AnnotationsProcessor.getAnnotation(node, REQUEST_MAPPING_ANNOTATIONS)
        String urlTemplate = mappingAnnotation ? getUrlTemplate(mappingAnnotation) : ""
        urlTemplate
    }

    static boolean hasSimpleUrlTemplate(Node node) {
        AnnotationExpr mappingAnnotation = AnnotationsProcessor.getAnnotation(node, REQUEST_MAPPING_ANNOTATIONS)
        if(!mappingAnnotation){
            return true
        }
        Optional<Expression> mappingExpression = AnnotationsProcessor.getAnnotationMemberExpressionValue(mappingAnnotation,
                [AnnotationsProcessor.DEFAULT_ANNOTATION_PROPERTY, PATH_PROPERTY])
        return (!mappingExpression.isPresent() || mappingExpression.get().isStringLiteralExpr())
    }
}
