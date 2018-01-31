package com.aurea.testgenerator.value;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public interface TestValue {
    String get();

    default ImmutableCollection<String> getImports() {
        return ImmutableList.of();
    }
}
