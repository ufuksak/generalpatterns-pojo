package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class RandomList implements TestValue {

    private final TestValue testValue;

    private RandomList(TestValue testValue) {
        this.testValue = testValue;
    }

    public static RandomList listOf(TestValue testValue) {
        return new RandomList(testValue);
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.<String>builder()
                .addAll(testValue.getImports())
                .add("static java.util.Collections.singletonList").build();
    }

    @Override
    public String get() {
        return "singletonList(" + testValue.get() + ")";
    }
}
