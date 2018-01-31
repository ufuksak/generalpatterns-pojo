package com.aurea.testgenerator.xml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

@Component
public class XPathEvaluatorImpl implements XPathEvaluator {

    private final XPath xPath;

    @Autowired
    public XPathEvaluatorImpl(XPath xPath) {
        this.xPath = xPath;
    }

    @Override
    public boolean is(Node node, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            return (boolean) xPathExpression.evaluate(node, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionException(e);
        }
    }

    @Override
    public String getText(Node node, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            return (String) xPathExpression.evaluate(node, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionException(e);
        }
    }

    @Override
    public int count(Node node, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            return ((Double) xPathExpression.evaluate(node, XPathConstants.NUMBER)).intValue();
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionException(e);
        }
    }

    @Override
    public List<Node> getNodes(Node node, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(node, XPathConstants.NODESET);
            List<Node> result = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                result.add(nodeList.item(i));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionException(e);
        }
    }

    @Override
    public Node getNode(Node node, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            return (Node) xPathExpression.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new InvalidXPathExpressionException(e);
        }
    }
}
