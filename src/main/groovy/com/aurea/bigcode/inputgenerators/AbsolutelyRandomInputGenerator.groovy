package com.aurea.bigcode.inputgenerators


import com.aurea.bigcode.TestedMethod
import com.aurea.bigcode.Value
import com.aurea.bigcode.executors.MethodInput
import com.aurea.testgenerator.value.random.RandomValueFactory


class AbsolutelyRandomInputGenerator implements InputGenerator {

    RandomValueFactory randoms = new RandomValueFactory()

    @Override
    MethodInput next(TestedMethod method) {
        List<Value> randomValues = method.declaration.parameters.collect {
            String valueAsAString = randoms.get(it.type.toString()).get()
            new Value(
                    type: it.type,
                    value: valueAsAString,
                    snippet: valueAsAString
            )
        }
        MethodInput.ofValues(randomValues)
    }
}
