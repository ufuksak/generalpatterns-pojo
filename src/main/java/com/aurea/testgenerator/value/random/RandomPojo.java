package com.aurea.testgenerator.value.random;

import com.aurea.common.ParsingUtils;
import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import static com.aurea.common.ParsingUtils.isFullName;

public class RandomPojo implements TestValue {

    private final String type;

    private RandomPojo(String type) {
        this.type = "Int".equals(type) ? "Integer" : type;
    }

    public static RandomPojo of(String type) {
        return new RandomPojo(type);
    }

    @Override
    public ImmutableCollection<String> getImports() {
        if (isFullName(type)) {
            return ImmutableList.of(type);
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public String get() {
        return "pojoFactory.manufacturePojo(" + asSimpleName(type) + ".class)";
    }

    private String asSimpleName(String type) {
        return ParsingUtils.parseSimpleName(type);
    }
}
