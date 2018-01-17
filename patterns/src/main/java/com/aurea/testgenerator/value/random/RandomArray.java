package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class RandomArray implements TestValue {

    private final String type;
    private final TestValue randomValue;

    private RandomArray(String type, TestValue randomValue) {
        this.type = "Int".equals(type) ? "Integer" : type;
        this.randomValue = randomValue;
    }

    public static RandomArray arrayOf(String type, TestValue randomValue) {
        return new RandomArray(type, randomValue);
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.<String>builder().addAll(getImports()).add(type).build();
    }

    @Override
    public String get() {
        return "new " + type + "[] { " + randomValue.get() + " }";
    }
}
