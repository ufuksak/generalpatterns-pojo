package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class CoverageReporter {

    ApplicationEventPublisher publisher

    @Autowired
    CoverageReporter(ApplicationEventPublisher publisher) {
        this.publisher = publisher
    }

    void report(Unit unit, TestGeneratorResult result, CallableDeclaration callable) {
        report(unit, result, Collections.singletonList(callable))
    }

    void report(Unit unit, TestGeneratorResult result, List<CallableDeclaration> callables) {
        if (result.tests) {
            publisher.publishEvent(new CoverageReportEvent(this, unit, callables, CoverageReportEventType.COVERED))
        } else {
            publisher.publishEvent(new CoverageReportEvent(this, unit, callables, CoverageReportEventType.MISSED))
        }
    }

    void reportResolved(Unit unit, TestGeneratorResult result, List<ResolvedMethodDeclaration> resolvedMethods) {
        List<MethodDeclaration> visitedMethods = StreamEx.of(resolvedMethods).select(JavaParserMethodDeclaration).map {
            it.wrappedNode
        }.toList()
        report(unit, result, visitedMethods)
    }

    void reportFailure(Unit unit, CallableDeclaration visitedNode) {
        publisher.publishEvent(new CoverageReportEvent(this, unit, visitedNode, CoverageReportEventType.FAILED_TO_COVER))
    }

    void reportNotCovered(Unit unit, CallableDeclaration visitedNode) {
        publisher.publishEvent(new CoverageReportEvent(this, unit, visitedNode, CoverageReportEventType.NOT_COVERED))
    }
}
