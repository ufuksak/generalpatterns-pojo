package com.aurea.testgenerator.pattern.general

import com.aurea.testgenerator.pattern.PatternMatchImpl
import com.aurea.testgenerator.pattern.PatternType
import com.aurea.testgenerator.source.Unit


class PureFunctionMatch extends PatternMatchImpl {
    PureFunctionMatch(Unit unit, String methodName) {
        super(unit, methodName)
    }

    @Override
    PatternType type() {
        return null
    }
}
