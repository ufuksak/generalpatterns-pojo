package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade


class TestGeneratorVisitor extends VoidVisitorAdapter<JavaParserFacade> {
    List<TestGeneratorResult> results = []
    Unit unit
    JavaParserFacade solver

    TestGeneratorVisitor(Unit unit, JavaParserFacade solver) {
        this.unit = unit
        this.solver = solver
    }

    Collection<TestGeneratorResult> visit() {
        this.visit(unit.cu, solver)
        results
    }
}
