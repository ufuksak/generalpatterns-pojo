package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;

public class RandomString implements TestValue {

    @Override
    public String get() {
        return "\"" + RandomStringPool.next() + "\"";
    }
}
