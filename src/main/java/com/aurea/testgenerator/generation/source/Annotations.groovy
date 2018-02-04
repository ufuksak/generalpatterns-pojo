package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.AnnotationExpr


class Annotations {
    static final AnnotationExpr JUNIT_PARAMS = JavaParser.parseAnnotation("@RunWith(JUnitParams.class)")
    static final AnnotationExpr TEST = JavaParser.parseAnnotation("@Test")
    static final AnnotationExpr POWER_MOCK_RUNNER = JavaParser.parseAnnotation("@RunWith(PowerMockRunner.class)")
}
