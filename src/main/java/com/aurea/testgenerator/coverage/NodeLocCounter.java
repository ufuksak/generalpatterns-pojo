package com.aurea.testgenerator.coverage;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Taken from loc-counter with little to no refactoring:
 * https://github.com/trilogy-group/loc-counter/blob/master/src/main/java/com/aurea/loccounter/implementation/NodeLocCounter.java
 */
public class NodeLocCounter {

    private static final List<Predicate<Node>> IS_INSIGNIFICANT_BLOCKSTMT_CONDITIONS = Arrays
            .asList(
                    p -> p instanceof MethodDeclaration && !((MethodDeclaration) p).getType().isVoidType(),
                    p -> p instanceof TryStmt,
                    p -> p instanceof ForStmt,
                    p -> p instanceof IfStmt,
                    p -> p instanceof WhileStmt,
                    p -> p instanceof DoStmt
            );

    public static long count(Node node) {
        return count(node.getChildNodes());
    }

    public static long count(List<Node> nodes) {
        long count = countRecursively(nodes);
        count += nodes.stream().map(Node::getChildNodes).mapToLong(NodeLocCounter::count).sum();
        return count;
    }

    private static long countRecursively(List<Node> nodes) {
        long statements = significantStatements(nodes).count();
        long blockStatements = blockStatements(nodes).mapToLong(NodeLocCounter::calculateBlockStmt).sum();

        final Map<Boolean, List<VariableDeclarator>> variablesByScope = variableDeclarators(nodes)
                .collect(Collectors.partitioningBy(NodeLocCounter::isField));
        final List<VariableDeclarator> fieldDeclarations = variablesByScope.get(true);
        final List<VariableDeclarator> variableDeclarations = variablesByScope.get(false);
        long fields = fieldDeclarations.stream()
                .filter(NodeLocCounter::isSignificantFieldDeclaration).count();
        long insignificantVariables = variableDeclarations.stream()
                .filter(NodeLocCounter::isInsignficantVariableDeclaration).count();
        return statements + blockStatements + fields - insignificantVariables;
    }

    private static Stream<Node> significantStatements(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof Statement &&
                        !(x instanceof BlockStmt) &&
                        !(x instanceof TryStmt));
    }

    private static Stream<BlockStmt> blockStatements(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof BlockStmt)
                .map(BlockStmt.class::cast);
    }

    private static Stream<VariableDeclarator> variableDeclarators(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof VariableDeclarator)
                .map(VariableDeclarator.class::cast);
    }

    private static boolean isSignificantFieldDeclaration(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getInitializer().isPresent() && !isClassConstant(variableDeclarator);
    }

    private static boolean isInsignficantVariableDeclaration(VariableDeclarator variableDeclarator) {
        return !variableDeclarator.getInitializer().isPresent() &&
                variableDeclarator.getParentNode()
                        .filter(VariableDeclarationExpr.class::isInstance)
                        .flatMap(Node::getParentNode)
                        .map(ExpressionStmt.class::isInstance).orElse(false);
    }

    private static boolean isClassConstant(VariableDeclarator variableDeclarator) {
        return isFinalStaticField(variableDeclarator) && hasLiteralInitializer(variableDeclarator);
    }

    private static boolean isField(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getParentNode()
                .map(FieldDeclaration.class::isInstance)
                .orElse(false);
    }

    private static Boolean isFinalStaticField(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getParentNode()
                .filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast)
                .map(fd -> fd.isFinal() && fd.isStatic())
                .orElse(false);
    }

    private static boolean hasLiteralInitializer(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getInitializer()
                .map(LiteralExpr.class::isInstance)
                .orElse(false);
    }

    private static long calculateBlockStmt(BlockStmt blockStmt) {
        return blockStmt.getParentNode()
                .map(parent -> IS_INSIGNIFICANT_BLOCKSTMT_CONDITIONS.stream()
                        .anyMatch(condition -> condition.test(parent)) ? 0L : 1L)
                .orElse(1L);
    }
}
