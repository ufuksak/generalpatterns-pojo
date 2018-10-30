package com.aurea.testgenerator.generation.names.builder

import com.aurea.testgenerator.generation.names.TestClassNomenclature
import com.aurea.testgenerator.source.Unit


class BuilderTestClassNomenclature implements TestClassNomenclature {
    @Override
    String requestTestClassName(Unit unit) {
        unit.className + "Test"
    }
}
