package com.aurea.testgenerator.ast;

import com.github.javaparser.ast.Modifier;
import com.google.common.base.Joiner;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class NodeUtils {
    private NodeUtils() {
    }

    public static String buildUpdateObjectClassName(Node updateReferenceType) {
        List<String> nameParts = new ArrayList<>();
        addClassOrInterfaceTypeName(updateReferenceType, nameParts);
        return Joiner.on(".").join(nameParts);
    }

    private static void addClassOrInterfaceTypeName(Node node, List<String> nameParts) {
        Node firstChild = node.getChildNodes().item(1);
        if (null != firstChild && firstChild.getNodeName().equals("ClassOrInterfaceType")) {
            addClassOrInterfaceTypeName(firstChild, nameParts);
        }
        Node name = node.getAttributes().getNamedItem("name");
        if (null != name) {
            nameParts.add(name.getNodeValue());
        }
    }

    public static int toModifiers(EnumSet<Modifier> modifiers) {
        int i = 0;
        for (Modifier modifier : modifiers) {
            switch (modifier) {
                case PUBLIC:
                    i += java.lang.reflect.Modifier.PUBLIC;
                    break;
                case PROTECTED:
                    i += java.lang.reflect.Modifier.PROTECTED;
                    break;
                case PRIVATE:
                    i += java.lang.reflect.Modifier.PRIVATE;
                    break;
                case ABSTRACT:
                    i += java.lang.reflect.Modifier.ABSTRACT;
                    break;
                case STATIC:
                    i += java.lang.reflect.Modifier.STATIC;
                    break;
                case FINAL:
                    i += java.lang.reflect.Modifier.FINAL;
                    break;
                case TRANSIENT:
                    i += java.lang.reflect.Modifier.TRANSIENT;
                    break;
                case VOLATILE:
                    i += java.lang.reflect.Modifier.VOLATILE;
                    break;
                case SYNCHRONIZED:
                    i += java.lang.reflect.Modifier.SYNCHRONIZED;
                    break;
                case NATIVE:
                    i += java.lang.reflect.Modifier.NATIVE;
                    break;
                case STRICTFP:
                    i += java.lang.reflect.Modifier.STRICT;
                    break;
                case TRANSITIVE:
                    i += java.lang.reflect.Modifier.TRANSIENT;
                    break;
            }
        }
        return i;
    }
}
