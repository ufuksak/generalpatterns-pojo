package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;

import static org.apache.commons.lang3.RandomUtils.nextInt;

public class RandomTimestamp implements TestValue {
    @Override
    public String get() {
        int i = nextInt(15, 77);
        return "new Timestamp(" + i + "L)";
    }
}
