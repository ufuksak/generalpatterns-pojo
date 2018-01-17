package com.aurea.testgenerator.pattern.general;

import com.aurea.testgenerator.pattern.PatternMatchImpl;
import com.aurea.testgenerator.pattern.PatternType;
import com.aurea.testgenerator.source.Unit;

public class PureFunctionMatch extends PatternMatchImpl {
    public PureFunctionMatch(Unit unit, String methodName) {
        super(unit, methodName);
    }

    @Override
    public PatternType type() {
        return null;
    }
}
