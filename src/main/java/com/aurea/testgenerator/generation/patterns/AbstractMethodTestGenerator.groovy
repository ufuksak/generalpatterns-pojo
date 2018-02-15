package com.aurea.testgenerator.generation.patterns

import com.aurea.testgenerator.generation.TestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
abstract class AbstractMethodTestGenerator implements TestGenerator {

    @Autowired
    JavaParserFacade solver

    @Autowired
    TestGeneratorResultReporter reporter

    @Autowired
    VisitReporter visitReporter

    @Autowired
    NomenclatureFactory nomenclatures

    @Override
    Collection<TestGeneratorResult> generate(Unit unit) {
        List<TestGeneratorResult> results = []
        new VoidVisitorAdapter<JavaParserFacade>() {
            @Override
            void visit(MethodDeclaration n, JavaParserFacade arg) {
                if (shouldBeVisited(unit, n)) {
                    try {
                        TestGeneratorResult result = generate(n, unit)
                        if (!result.type) {
                            result.type = getType()
                        }
                        reporter.publish(result, unit, n)
                        visitReporter.publishSuccessVisit(unit, n)
                        results << result
                    } catch (Exception e) {
                        log.error "Unhandled error while generating for $unit.fullName", e
                        visitReporter.publishFailedVisit(unit, n)
                    }
                } else {
                    visitReporter.publishSkippedVisit(unit, n)
                }
            }
        }.visit(unit.cu, solver)
        results
    }

    protected abstract TestGeneratorResult generate(MethodDeclaration method, Unit unitUnderTest)

    protected abstract TestType getType()

    protected boolean shouldBeVisited(Unit unit, MethodDeclaration method) {
        true
    }
}

