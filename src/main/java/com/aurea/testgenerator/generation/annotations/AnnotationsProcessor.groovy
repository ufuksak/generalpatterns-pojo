package com.aurea.testgenerator.generation.annotations

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations

class AnnotationsProcessor {

    public static final String DEFAULT_ANNOTATION_PROPERTY = "value"

    static String getStringValue(AnnotationExpr annotation, String memberName) {
        getAnnotationMemberExpressionValue(annotation, memberName)
                .map(this.&getAnnotationMemberValueAsString)
                .orElse("")
    }

    static Optional<Expression> getAnnotationMemberExpressionValue(AnnotationExpr annotation, List<String> memberNames) {
        memberNames.stream()
                .map { getAnnotationMemberExpressionValue(annotation, it) }
                .filter { it.present }
                .findFirst().orElse(Optional.empty())
    }

    static Optional<Expression> getAnnotationMemberExpressionValue(AnnotationExpr annotation, String memberName) {
        if (annotation.isSingleMemberAnnotationExpr() && memberName == DEFAULT_ANNOTATION_PROPERTY) {
            return Optional.of(annotation.asSingleMemberAnnotationExpr().memberValue)
        } else if (annotation.isNormalAnnotationExpr()) {
            return annotation.asNormalAnnotationExpr().pairs.stream()
                    .filter { it.nameAsString == memberName }
                    .map { it.value }
                    .findFirst()
        }
        return Optional.empty()
    }

    static getAnnotationMemberValueAsString(Expression memberExpression) {

        if (memberExpression.isStringLiteralExpr()) {
            memberExpression.asStringLiteralExpr().asString()
        } else if (memberExpression.isFieldAccessExpr()) {
            memberExpression.asFieldAccessExpr().toString()
        } else if (memberExpression.isArrayInitializerExpr()) {
            Optional.ofNullable(memberExpression.childNodes.first())
                    .map{getAnnotationMemberValueAsString(it)}
                    .orElse("")
        } else if (memberExpression.isNameExpr()) {
            memberExpression.asNameExpr().nameAsString
        } else {
            throw new IllegalArgumentException("Unknown annotation member expression type: $memberExpression")
        }
    }

    static String getStringValue(AnnotationExpr annotation, Set<String> memberNames) {
        memberNames.stream().map{getStringValue(annotation, it)}
                .filter{! it.isEmpty()}.findFirst().orElse("")
    }

    static boolean hasAnnotation(NodeWithAnnotations node, Set<String> annotationNames) {
        node.annotations.any { annotationNames.contains(it.nameAsString) }
    }

    static boolean hasAnnotation(NodeWithAnnotations node, String annotationName) {
        hasAnnotation(node, Collections.singleton(annotationName))
    }

    static AnnotationExpr getAnnotation(NodeWithAnnotations node, Set<String> annotationNames) {
        return node.annotations.find { annotationNames.contains(it.nameAsString) }
    }

    static AnnotationExpr getAnnotation(NodeWithAnnotations node, String annotationName) {
        return getAnnotation(node, Collections.singleton(annotationName))
    }
}
