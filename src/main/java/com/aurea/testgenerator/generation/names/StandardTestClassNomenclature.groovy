package com.aurea.testgenerator.generation.names

import com.aurea.testgenerator.source.Unit


class StandardTestClassNomenclature implements TestClassNomenclature {
    @Override
    String requestTestClassName(Unit unit) {
        unit.className + "Test"
    }
}
