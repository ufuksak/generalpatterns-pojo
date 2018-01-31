package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class RandomSet implements TestValue {

    private final TestValue testValue;

    private RandomSet(TestValue testValue) {
        this.testValue = testValue;
    }

    public static RandomSet setOf(TestValue testValue) {
        return new RandomSet(testValue);
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.<String>builder()
                .addAll(testValue.getImports())
                .add("static java.util.Collections.emptySet").build();
    }

    @Override
    public String get() {
        return "emptySet(" + testValue.get() + ")";
    }
}
