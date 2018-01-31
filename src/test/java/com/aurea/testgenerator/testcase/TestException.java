package com.aurea.testgenerator.testcase;

public class TestException extends Exception {

    private static final long serialVersionUID = 4938581631182355996L;

    public TestException() {
        super();
    }

    public TestException(String message) {
        super(message);
    }

    public TestException(String message, Exception e) {
        super(message, e);
    }
}