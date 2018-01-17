package com.aurea.testgenerator.value.arbitrary;

import com.aurea.testgenerator.value.arbitrary.ArbitraryInstance;
import com.github.javaparser.ast.body.Parameter;
import one.util.streamex.StreamEx;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodCallArgs {
    private Map<Parameter, ArbitraryInstance> parameters = new LinkedHashMap<>();

    public void addValue(Parameter parameter, ArbitraryInstance instance) {
        parameters.put(parameter, instance);
    }

    public void addValue(Parameter parameter, ArrayArbitraryInstance arrayArbitraryInstance) {
        parameters.put(parameter, arrayArbitraryInstance);
    }

    public ArbitraryInstance getParameter(Parameter p) {
        return parameters.get(p);
    }

    public StreamEx<ArbitraryInstance> values() {
        return StreamEx.of(parameters.values());
    }
}
