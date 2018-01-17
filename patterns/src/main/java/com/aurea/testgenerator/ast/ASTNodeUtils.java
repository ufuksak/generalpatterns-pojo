package com.aurea.testgenerator.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jasongoodwin.monads.Try;
import one.util.streamex.StreamEx;

public final class ASTNodeUtils {
    private ASTNodeUtils() {
    }

    public static <T> T findParentOf(Class<T> clazz, Node parent) {
        if (parent.getClass().equals(clazz)) {
            return clazz.cast(parent);
        }

        T result = null;
        while (parent.getParentNode().isPresent() && result == null) {
            Optional<Node> optionalParent = parent.getParentNode();
            result = findParentOf(clazz, optionalParent.get());
            parent = optionalParent.get();
        }
        return result;
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

    public static VariableDeclarationExpr findVariableDeclarationByName(String variableName, Node parent) {
        if (parent instanceof VariableDeclarationExpr) {
            VariableDeclarator variableDeclarator = findVariableDeclarator(parent);
            if (null != variableDeclarator) {
                if (variableDeclarator.getNameAsString().equals(variableName)) {
                    return (VariableDeclarationExpr) parent;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        List<Node> children = parent.getChildNodes();
        for (Node node : children) {
            VariableDeclarationExpr result = findVariableDeclarationByName(variableName, node);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static VariableDeclarator findVariableDeclarator(Node parent) {
        for (Node child : parent.getChildNodes()) {
            if (child instanceof VariableDeclarator) {
                return (VariableDeclarator) child;
            }
        }
        return null;
    }

    public static <T> List<T> findChildsOf(Class<T> clazz, Node parent) {
        return findChildsOf(clazz, parent, new ArrayList<>());
    }

    public static <T> Optional<T> findChildOf(Class<T> clazz, Node parent) {
        List<T> childsOf = findChildsOf(clazz, parent, new ArrayList<>());
        return childsOf.isEmpty() ? Optional.empty() : Optional.of(childsOf.get(0));
    }

    public static <T> Optional<T> findChildSubTypeOf(Class<T> clazz, Node parent) {
        List<T> childsOf = findChildsSubTypesOf(clazz, parent, new ArrayList<>());
        return childsOf.isEmpty() ? Optional.empty() : Optional.of(childsOf.get(0));
    }

    public static <T> List<T> findChildsSubTypesOf(Class<T> clazz, Node parent) {
        return findChildsSubTypesOf(clazz, parent, new ArrayList<T>());
    }

    public static <T> List<T> findDirectChildsOf(Class<T> clazz, Node parent) {
        return StreamEx.of(parent.getChildNodes()).filter(p -> p.getClass().equals(clazz)).map(clazz::cast).toList();
    }

    private static <T> List<T> findChildsOf(Class<T> clazz, Node node, List<T> childs) {
        if (node.getClass().equals(clazz)) {
            childs.add(clazz.cast(node));
        }

        for (Node child : node.getChildNodes()) {
            findChildsOf(clazz, child, childs);
        }
        return childs;
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

    public static Node hasNamedExpr(Node node, NameExpr nameExpr) {
        if (node.equals(nameExpr)) {
            return node.getParentNode().get();
        }
        for (Node child : node.getChildNodes()) {
            Node result = hasNamedExpr(child, nameExpr);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    public static List<AssignExpr> findAssignmentsForVariable(NameExpr variableName, Node parent) {
        return findAssignmentsForVariable(variableName, parent, new ArrayList<>());
    }

    private static List<AssignExpr> findAssignmentsForVariable(NameExpr variableName, Node node, ArrayList<AssignExpr> assignExprs) {
        if (node instanceof AssignExpr) {
            if (((AssignExpr) node).getTarget().equals(variableName)) {
                assignExprs.add((AssignExpr) node);
            }
        }

        for (Node child : node.getChildNodes()) {
            findAssignmentsForVariable(variableName, child, assignExprs);
        }
        return assignExprs;
    }

    public static List<MethodCallExpr> findMethodCallsForVariable(NameExpr variable, Node parent) {
        return findMethodCallsForVariable(variable, parent, new ArrayList<MethodCallExpr>());
    }

    private static List<MethodCallExpr> findMethodCallsForVariable(NameExpr variable, Node node, ArrayList<MethodCallExpr> methodCallExprs) {
        if (node instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) node;
            if (methodCallExpr.getScope() != null && methodCallExpr.getScope().equals(variable)) {
                methodCallExprs.add(methodCallExpr);
            }
        }

        for (Node child : node.getChildNodes()) {
            findMethodCallsForVariable(variable, child, methodCallExprs);
        }
        return methodCallExprs;
    }

    public static boolean isCollection(Type type) {
        if (type instanceof ReferenceType) {
            ReferenceType reference = (ReferenceType) type;
            if (reference instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) reference;
                return classOrInterfaceType.getNameAsString().contains("Collection") || classOrInterfaceType.getNameAsString().contains("List");
            }
        }
        return false;
    }

    public static List<String> getMethodArgs(MethodDeclaration n) {
        return n.getParameters().stream().map(p -> p.getType().toString()).collect(Collectors.toList());
    }

    public static String getImportByName(String name, CompilationUnit cu) {
        return cu.getImports().stream()
                .filter(id -> name.equals(id.getNameAsString()))
                .map(id -> ImportWrapper.asImportStatement(id.getName().toString()))
                .findFirst().orElse("");
    }

    public static String getFullClassName(String name, CompilationUnit cu) {
        return cu.getImports().stream()
                .filter(id -> id.getNameAsString().endsWith(name))
                .map(id -> id.getName().toString())
                .findFirst().orElse(name);
    }

    public static boolean returnsVoid(MethodDeclaration n) {
        return n.getType() instanceof VoidType;
    }

    public static <T, R> Optional<R> as(Class<T> safeCastClass, Node node, Function<T, R> function) {
        if (safeCastClass.isInstance(node)) {
            T casted = safeCastClass.cast(node);
            return Optional.of(function.apply(casted));
        }
        return Optional.empty();
    }

    public static int countNodes(MethodDeclaration n) {
        LongAdder accumulator = new LongAdder();
        countNodes(n, accumulator);
        return accumulator.intValue();
    }

    private static void countNodes(Node n, LongAdder accumulator) {
        accumulator.increment();
        for (Node node : n.getChildNodes()) {
            countNodes(node, accumulator);
        }
    }

    public static List<Node> getAllChilds(Node n) {
        List<Node> accumulator = new ArrayList<>();
        getAllChilds(n, accumulator);
        return accumulator;
    }

    private static void getAllChilds(Node n, List<Node> accumulator) {
        for (Node node: n.getChildNodes()) {
            accumulator.add(node);
            getAllChilds(node, accumulator);
        }
    }

    public static boolean hasOnlyChildsOf(Node n, Class<? extends Node> one) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one);
        return hasOnlyNodesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsOf(Node n,  Class<? extends Node> one, Class<? extends Node> two) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one, two);
        return hasOnlyNodesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsSubTypesOf(Node n, Class<? extends Node> one) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one);
        return hasOnlyChildsSubTypesOf(n, allowedClasses);
    }

    public static boolean hasOnlyChildsSubTypesOf(Node n,  Class<? extends Node> one, Class<? extends Node> two) {
        Set<Class<? extends Node>> allowedClasses = ImmutableSet.of(one, two);
        return hasOnlyChildsSubTypesOf(n, allowedClasses);
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

    public static String getFullName(ClassOrInterfaceDeclaration coid) {
        CompilationUnit cu = findParentOf(CompilationUnit.class, coid);
        Optional<PackageDeclaration> packageDeclaration = findChildOf(PackageDeclaration.class, cu);
        return packageDeclaration
                .map(pd -> pd.getNameAsString() + "." + coid.getNameAsString())
                .orElse("");
    }

    public static StreamEx<AssignExpr> findFieldAssignmentsInGivenNodeByName(Node node, SimpleName name) {
        return StreamEx.of(node.getNodesByType(AssignExpr.class)).filter(ae -> {
            if (NameExpr.class.isAssignableFrom(ae.getTarget().getClass())) {
                return ((NameExpr)ae.getTarget()).getName().equals(name);
            } else if (FieldAccessExpr.class.isAssignableFrom(ae.getTarget().getClass())) {
                return ((FieldAccessExpr)ae.getTarget()).getName().equals(name);
            }
            return false;
        });
    }

    public static MethodDeclaration findMethodOrFail(ClassOrInterfaceDeclaration n, final String name) {
        return Try.ofFailable(() -> n.getMethodsByName(name).get(0)).orElseThrow(() -> new MethodDeclarationNotFoundException(
                "Failed to find method '" + name + "' in '" + n.getNameAsString() + "'"));
    }

    public static MethodDeclaration findMethodOrNull(ClassOrInterfaceDeclaration n, final String name) {
        return Try.ofFailable(() -> n.getMethodsByName(name).get(0)).orElse(null);
    }

    public static VariableDeclarator findFieldVariableOrFail(ClassOrInterfaceDeclaration n, final String name) {
        return Try.ofFailable(() -> StreamEx.of(n.getFields()).flatMap( it -> it.getVariables().stream()).findAny( it -> it.getNameAsString().equals(name)).get())
                .orElseThrow(() -> new FieldVariableDeclaratorNotFoundException("Failed to find variable declaration '" + name + "' in '" + n.getNameAsString() + "'"));
    }

    public static VariableDeclarator findFieldVariableOrNull(ClassOrInterfaceDeclaration n, final String name) {
        return Try.ofFailable(() -> StreamEx.of(n.getFields()).flatMap(it -> it.getVariables().stream()).findAny(it -> it.getNameAsString().equals(name)).get())
                .orElse(null);
    }

    public static StreamEx<Node> findParents(Node childNode) {
        List<Node> nodes = new ArrayList<>();

        Optional<Node> node = childNode.getParentNode();

        while(node.isPresent()) {

            nodes.add(node.get());

            node = node.get().getParentNode();
        }

        return StreamEx.of(nodes);
    }
    public static  <T extends Node> StreamEx<T> findParents(Node childNode, Class<T> clazzOfParent) {

        return findParents(childNode).select(clazzOfParent);
    }

    public static <T extends Node> StreamEx<T> findParents(Node childNode, Class<T> clazzOfParent, Predicate<T> condition) {

        return findParents(childNode, clazzOfParent).filter(condition);

    }
}
