package com.aurea.testgenerator.generation.source

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.AnnotationExpr


class Annotations {
    static final AnnotationExpr JUNIT_PARAMS = JavaParser.parseExpression("@RunWith(JUnitParams.class)").asAnnotationExpr()
    static final AnnotationExpr TEST = JavaParser.parseExpression("@Test").asAnnotationExpr()
    static final AnnotationExpr POWER_MOCK_RUNNER = JavaParser.parseExpression("@RunWith(PowerMockRunner.class)").asAnnotationExpr()

}
