package com.aurea.testgenerator.generation.annotations

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Expression

class AnnotationsProcessor {

    public static final String DEFAULT_ANNOTATION_PROPERTY = "value"

    static String getStringValue(AnnotationExpr annotation, String memberName) {
        Optional<Expression> memberExpression = getAnnotationMemberExpressionValue(annotation, memberName)
        if (memberExpression.isPresent()) {
            return getAnnotationMemberValueAsString(memberExpression.get())
        }
        return ""
    }

    static Optional<Expression> getAnnotationMemberExpressionValue(AnnotationExpr annotation, List<String> memberNames) {
        memberNames.forEach {
            Optional<Expression> expression = getAnnotationMemberExpressionValue(annotation, it)
            if (expression.isPresent()) {
                return expression
            }
        }
        return Optional.empty()
    }

    static Optional<Expression> getAnnotationMemberExpressionValue(AnnotationExpr annotation, String memberName) {
        if (annotation.isSingleMemberAnnotationExpr() && memberName == DEFAULT_ANNOTATION_PROPERTY) {
            return Optional.of(annotation.asSingleMemberAnnotationExpr().memberValue)
        } else if (annotation.isNormalAnnotationExpr()) {
            def pair = annotation.asNormalAnnotationExpr().pairs.find { it.nameAsString == memberName }
            if (pair) {
                return Optional.of(pair.value)
            }
        }
        return Optional.empty()
    }

    static getAnnotationMemberValueAsString(Expression memberExpression){
        if(memberExpression.isStringLiteralExpr()){
            memberExpression.asStringLiteralExpr().asString()
        }else if(memberExpression.isFieldAccessExpr()){
            memberExpression.asFieldAccessExpr().toString()
        }else if(memberExpression.isArrayInitializerExpr()){
            List<Node> childNodes = memberExpression.childNodes
            if (childNodes.isEmpty()){
                ""
            }else {
                getAnnotationMemberValueAsString(childNodes.first() as Expression)
            }
        }else  if(memberExpression.isNameExpr()){
            memberExpression.asNameExpr().nameAsString
        }
        else {
            throw new IllegalArgumentException("Unknown annotation member expression type: $memberExpression")
        }
    }

    static String getStringValue(AnnotationExpr annotation, Set<String> memberNames){
        for(String memberName: memberNames){
            String value = getStringValue(annotation, memberName)
            if(!value.isEmpty()){
                return value
            }
        }
        return ""
    }

    static boolean hasAnnotation(Node node, Set<String> annotatioNames) {
        node.annotations.any { annotatioNames.contains(it.nameAsString) }
    }

    static boolean hasAnnotation(Node node, String annotatioName) {
        hasAnnotation(node, Collections.singleton(annotatioName))
    }

    static AnnotationExpr getAnnotation(Node node, Set<String> annotatioNames) {
        List<AnnotationExpr> annotations = node.annotations
        return annotations.find { annotatioNames.contains(it.nameAsString) }
    }

    static AnnotationExpr getAnnotation(Node node, String annotatioName) {
        return getAnnotation(node, Collections.singleton(annotatioName))
    }
}
