package com.aurea.testgenerator.value;

import static com.aurea.testgenerator.source.ParsingUtils.parseSimpleName;

import com.aurea.testgenerator.source.ParsingUtils;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class MockValue implements TestValue {

    private final String fullyQualifiedName;

    public MockValue(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    @Override
    public String get() {
        return "mock(" + parseSimpleName(fullyQualifiedName) + ".class)";
    }

    @Override
    public ImmutableCollection<String> getImports() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("static org.mockito.Mockito.mock");
        if (ParsingUtils.isFullName(fullyQualifiedName)) {
            builder.add(fullyQualifiedName);
        }
        return builder.build();
    }
}
