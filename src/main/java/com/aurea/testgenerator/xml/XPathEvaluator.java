package com.aurea.testgenerator.xml;

import org.w3c.dom.Node;

import java.util.List;

/**
 * Wraps XPath compiling and evaluation. Throws unchecked InvalidXPathExpression in case of invalid query.
 */
public interface XPathEvaluator {

    boolean is(Node node, String expression);

    String getText(Node node, String expression);

    int count(Node node, String expression);

    List<Node> getNodes(Node node, String expression);

    Node getNode(Node node, String expression);
}

