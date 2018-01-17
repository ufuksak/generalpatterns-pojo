package com.aurea.testgenerator.pattern;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExceptionTest {

    @Test
    public void test_constructorNoArg_Always_ShouldSetNullOrEmptyMessage() throws Exception {
        TestException exception = new TestException();

        assertThat(exception.getMessage()).isNullOrEmpty();
    }

    @Test
    public void test_constructorMessageArg_Always_ShouldSetMessage() throws Exception {
        TestException exception = new TestException("test message");

        assertThat(exception.getMessage()).isEqualTo("test message");
    }

    @Test
    public void test_constructorMessageArgAndCauseArg_Always_ShouldSetMessageAndCause() throws Exception {
        Exception cause = new Exception("cause");
        TestException exception = new TestException("test message", cause);

        assertThat(exception.getMessage()).isEqualTo("test message");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
