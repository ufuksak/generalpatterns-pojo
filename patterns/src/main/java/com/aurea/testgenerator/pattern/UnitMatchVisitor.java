package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.Collection;

public abstract class UnitMatchVisitor extends VoidVisitorAdapter<JavaParserFacade> implements MatchVisitor {

    protected final Collection<PatternMatch> matches;
    protected final Unit unit;
    protected final JavaParserFacade solver;

    public UnitMatchVisitor(Unit unit) {
        this(unit, null);
    }

    public UnitMatchVisitor(Unit unit, JavaParserFacade solver) {
        this.unit = unit;
        this.solver = solver;
        matches = new ArrayList<>();
    }

    public Unit getUnit() {
        return unit;
    }

    public void visit() {
        this.visit(unit.getCu(), solver);
    }

    public StreamEx<PatternMatch> matches() {
        return StreamEx.of(matches);
    }
}
