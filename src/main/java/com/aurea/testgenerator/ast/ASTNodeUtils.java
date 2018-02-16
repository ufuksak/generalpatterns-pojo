package com.aurea.testgenerator.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ImmutableSet;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;

public final class ASTNodeUtils {

    private static final CompilationUnit UNKNOWN_CU = new CompilationUnit("unknown");

    static {
        UNKNOWN_CU.addType(new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "UnknownClass"));
    }

    private ASTNodeUtils() {
    }

    public static <T> T findParentSubTypeOf(Class<T> clazz, Node parent) {
        if (clazz.isAssignableFrom(parent.getClass())) {
            return clazz.cast(parent);
        }

        T result = null;
        while (parent.getParentNode().isPresent() && result == null) {
            result = findParentSubTypeOf(clazz, parent.getParentNode().get());
            parent = parent.getParentNode().get();
        }
        return result;
    }

    public static <T> T findNextParentSubTypeOf(Class<T> clazz, Node parent) {
        T result = null;
        while (parent.getParentNode().isPresent() && result == null) {
            result = findParentSubTypeOf(clazz, parent.getParentNode().get());
            parent = parent.getParentNode().get();
        }
        return result;
    }

    public static <T> Optional<T> findAncestorSubTypeOf(Class<T> clazz, Node node) {
        if (!node.getParentNode().isPresent()) {
            return Optional.empty();
        }
        return findAncestorSubTypeOf(clazz, node, null);
    }

    public static <T> List<T> findDirectChildsOf(Class<T> clazz, Node parent) {
        return StreamEx.of(parent.getChildNodes()).filter(p -> p.getClass().equals(clazz)).map(clazz::cast).toList();
    }

    public static <T> Optional<T> findChildSubTypeOf(Class<T> clazz, Node parent) {
        List<T> childsOf = findChildsSubTypesOf(clazz, parent, new ArrayList<>());
        return childsOf.isEmpty() ? Optional.empty() : Optional.of(childsOf.get(0));
    }

    public static <T> List<T> findChildsSubTypesOf(Class<T> clazz, Node parent) {
        return findChildsSubTypesOf(clazz, parent, new ArrayList<T>());
    }

    public static int countNodes(Node n) {
        LongAdder accumulator = new LongAdder();
        countNodes(n, accumulator);
        return accumulator.intValue();
    }

    public static List<Node> getAllChilds(Node n) {
        List<Node> accumulator = new ArrayList<>();
        getAllChilds(n, accumulator);
        return accumulator;
    }

    public static boolean hasOnlyChildsOf(Node n, Class<? extends Node> one) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one);
        return hasOnlyNodesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsOf(Node n, Class<? extends Node> one, Class<? extends Node> two) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one, two);
        return hasOnlyNodesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsSubTypesOf(Node n, Class<? extends Node> one) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one);
        return hasOnlyChildsSubTypesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsSubTypesOf(Node n, Class<? extends Node> one, Class<? extends Node> two) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one, two);
        return hasOnlyChildsSubTypesOf(n, allowedClasses);
    }

    public static String getNameOfCompilationUnit(Node n) {
        CompilationUnit compilationUnit = n.findCompilationUnit().orElse(UNKNOWN_CU);
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String typeName = compilationUnit.getType(0).getNameAsString();
        return packageName + "." + typeName;
    }

    public static StreamEx<Node> parents(Node childNode) {
        List<Node> nodes = new ArrayList<>();
        Optional<Node> node = childNode.getParentNode();
        while (node.isPresent()) {
            nodes.add(node.get());
            node = node.get().getParentNode();
        }
        return StreamEx.of(nodes);
    }

    public static <T extends Node> StreamEx<T> parents(Node childNode, Class<T> clazzOfParent) {
        return parents(childNode).select(clazzOfParent);
    }

    public static <T extends Node> StreamEx<T> parents(Node childNode, Class<T> clazzOfParent, Predicate<T> condition) {
        return parents(childNode, clazzOfParent).filter(condition);
    }

    public static String getFullTypeName(TypeDeclaration typeDeclaration) {
        List<String> types = new ArrayList<>();
        types.add(typeDeclaration.getNameAsString());
        parents(typeDeclaration, TypeDeclaration.class).forEach(parentType -> types.add(parentType.getNameAsString()));
        return StreamEx.ofReversed(types).joining(".");
    }

    private static <T> List<T> findChildsSubTypesOf(Class<T> clazz, Node node, List<T> childs) {
        if (clazz.isAssignableFrom(node.getClass())) {
            childs.add(clazz.cast(node));
        }

        for (Node child : node.getChildNodes()) {
            findChildsSubTypesOf(clazz, child, childs);
        }
        return childs;
    }

    private static <T> Optional<T> findAncestorSubTypeOf(Class<T> clazz, Node node, T candidate) {
        Optional<Node> maybeParent = node.getParentNode();
        if (!maybeParent.isPresent()) {
            return Optional.ofNullable(candidate);
        } else {
            Node parent = maybeParent.get();
            return findAncestorSubTypeOf(clazz, parent,
                    clazz.isAssignableFrom(parent.getClass()) ? clazz.cast(parent) : candidate);
        }
    }

    private static void countNodes(Node n, LongAdder accumulator) {
        accumulator.increment();
        for (Node node : n.getChildNodes()) {
            countNodes(node, accumulator);
        }
    }

    private static void getAllChilds(Node n, List<Node> accumulator) {
        for (Node node : n.getChildNodes()) {
            accumulator.add(node);
            getAllChilds(node, accumulator);
        }
    }

    private static boolean hasOnlyNodesOf(Node n, Set<Class<? extends Node>> allowedClasses) {
        for (Node node : n.getChildNodes()) {
            if (!allowedClasses.contains(node.getClass()) || !hasOnlyChildsSubTypesOf(node, allowedClasses)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasOnlyChildsSubTypesOf(Node n, Set<Class<? extends Node>> allowedClasses) {
        for (Node node : n.getChildNodes()) {
            if (!isAssignableFrom(allowedClasses, node.getClass()) || !hasOnlyChildsSubTypesOf(node, allowedClasses)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignableFrom(Collection<Class<? extends Node>> allowed, Class<? extends Node> clazz) {
        for (Class<? extends Node> aClass : allowed) {
            if (aClass.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
