package com.aurea.testgenerator.generation.names

import com.aurea.common.JavaClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap


@Component
class NomenclatureFactory {

    Map<JavaClass, TestMethodNomenclature> testMethodNomenclatures = new ConcurrentHashMap<>()
    Map<JavaClass, TestClassNomenclature> testClassNomenclatures = new ConcurrentHashMap<>()
    Map<JavaClass, Map<String, TestVariableNomenclature>> testVariablesNomenclatures = new ConcurrentHashMap<>()

    final TestClassNomenclatureFactory testClassNomenclatureFactory

    @Autowired
    NomenclatureFactory(TestClassNomenclatureFactory testClassNomenclatureFactory) {
        this.testClassNomenclatureFactory = testClassNomenclatureFactory
    }

    TestMethodNomenclature getTestMethodNomenclature(JavaClass javaClass) {
        testMethodNomenclatures.computeIfAbsent(javaClass, { new TestMethodNomenclature() })
    }

    TestVariableNomenclature getVariableNomenclature(JavaClass javaClass, String methodName) {
        Map<String, TestVariableNomenclature> variablesPerMethod = testVariablesNomenclatures.computeIfAbsent(javaClass, {
            new HashMap<>()
        })
        variablesPerMethod.computeIfAbsent(methodName, { new StandardVariableNomenclature() })
    }

    TestClassNomenclature getTestClassNomenclature(JavaClass javaClass) {
        testClassNomenclatures.computeIfAbsent(javaClass, { testClassNomenclatureFactory.newTestClassNomenclature() })
    }
}
