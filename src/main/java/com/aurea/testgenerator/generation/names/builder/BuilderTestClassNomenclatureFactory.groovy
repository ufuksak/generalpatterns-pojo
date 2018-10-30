package com.aurea.testgenerator.generation.names.builder

import com.aurea.testgenerator.generation.names.TestClassNomenclature
import com.aurea.testgenerator.generation.names.TestClassNomenclatureFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile('builder')
class BuilderTestClassNomenclatureFactory implements TestClassNomenclatureFactory {
    @Override
    TestClassNomenclature newTestClassNomenclature() {
        new BuilderTestClassNomenclature()
    }
}
