package com.aurea.sample;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class SampleTest {

    @Test
    @Parameters({
            "0|0|0",
            "0|1|1",
            "1|0|1",
            "3|2|5",
    })
    public void test_addNumbers(int a, int b, int c) throws Exception {
        assertEquals(c, Sample.addNumbers(a, b));
    }
}
