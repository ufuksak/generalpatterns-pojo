package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.CallableDeclaration
import org.springframework.context.ApplicationEvent

class CoverageReportEvent extends ApplicationEvent {
    List<CallableDeclaration> callables
    Unit unit
    CoverageReportEventType eventType

    CoverageReportEvent(Object source, Unit unit, CallableDeclaration callable, CoverageReportEventType eventType) {
        super(source)
        this.unit = unit
        this.callables = Collections.singletonList(callable)
        this.eventType = eventType
    }

    CoverageReportEvent(Object source, Unit unit, List<CallableDeclaration> callables, CoverageReportEventType eventType) {
        super(source)
        this.unit = unit
        this.callables = callables
        this.eventType = eventType
    }
}
