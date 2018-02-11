package com.aurea.testgenerator.generation.names

import com.aurea.common.JavaClass
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap


@Component
class NomenclatureFactory {

    Map<JavaClass, TestMethodNomenclature> testMethodNomenclatures = new ConcurrentHashMap<>()

    TestMethodNomenclature getTestMethodNomenclature(JavaClass javaClass) {
        testMethodNomenclatures.computeIfAbsent(javaClass, { new TestMethodNomenclature() })
    }
}
