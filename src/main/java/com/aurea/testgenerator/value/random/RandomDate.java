package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomUtils;

public class RandomDate implements TestValue {

    public static final long ALMOST_YEAR = 1000 * 60 * 60 * 24 * 300L;

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.of("java.util.Date");
    }

    public String get() {
        long randomness = RandomUtils.nextLong(0, ALMOST_YEAR);
        return "new Date(new Date().getTime() - " + String.valueOf(randomness) + "L)";
    }
}
