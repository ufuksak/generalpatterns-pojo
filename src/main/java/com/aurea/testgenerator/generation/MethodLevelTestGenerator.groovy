package com.aurea.testgenerator.generation

import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2

@Log4j2
abstract class MethodLevelTestGenerator<T extends CallableDeclaration> implements TestGenerator {

    protected JavaParserFacade solver

    protected TestGeneratorResultReporter reporter

    protected CoverageReporter coverageReporter

    protected NomenclatureFactory nomenclatures

    MethodLevelTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, CoverageReporter coverageReporter, NomenclatureFactory nomenclatures) {
        this.solver = solver
        this.reporter = reporter
        this.coverageReporter = coverageReporter
        this.nomenclatures = nomenclatures
    }

    Collection<TestGeneratorResult> generate(Unit unit) {
        List<TestGeneratorResult> results = []
        createVisitor(unit, results).visit(unit.cu, solver)
        results
    }

    void visit(T callableDeclaration, Unit unit, List<TestGeneratorResult> results) {
        if (shouldBeVisited(unit, callableDeclaration)) {
            try {
                TestGeneratorResult result = generate(callableDeclaration, unit)
                if (!result.type) {
                    result.type = getType()
                }
                reporter.publish(result, unit, callableDeclaration)
                coverageReporter.report(unit, result, callableDeclaration)
                results << result
            } catch (Exception e) {
                log.error "Unhandled error while generating for $unit.fullName", e
                coverageReporter.reportFailure(unit, callableDeclaration)
            }
        } else {
            coverageReporter.reportNotCovered(unit, callableDeclaration)
        }
    }

    protected abstract VoidVisitorAdapter<JavaParserFacade> createVisitor(Unit unit, List<TestGeneratorResult> results)

    protected boolean shouldBeVisited(Unit unit, T callableDeclaration) {
        true
    }

    protected abstract TestGeneratorResult generate(T callableDeclaration, Unit unitUnderTest)

    protected abstract TestType getType()
}
