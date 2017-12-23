package com.aurea.bigcode.executors

import com.aurea.bigcode.Value
import groovy.transform.Canonical


@Canonical
class MethodInput {
    public static final MethodInput NO_INPUT = ofValues()

    private final Collection<Value> values

    private MethodInput(Value[] values) {
        this.values = Arrays.asList(values)
    }

    private MethodInput(List<Value> values) {
        this.values = new ArrayList<>(values)
    }

    static MethodInput ofValues(Value... values) {
        return new MethodInput(values)
    }

    static MethodInput ofValues(List<Value> values) {
        return new MethodInput(values)
    }

    @Override
    String toString() {
        return values.toString()
    }
}
