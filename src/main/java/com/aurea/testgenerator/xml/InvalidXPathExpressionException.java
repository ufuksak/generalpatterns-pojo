package com.aurea.testgenerator.xml;

import javax.xml.xpath.XPathExpressionException;

public class InvalidXPathExpressionException extends RuntimeException {
    public InvalidXPathExpressionException(XPathExpressionException e) {
        super(e);
    }
}
