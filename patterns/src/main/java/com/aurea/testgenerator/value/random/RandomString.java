package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class RandomString implements TestValue {

    @Override
    public String get() {
        return "\"" + RandomStringPool.next() + "\"";
    }
}
