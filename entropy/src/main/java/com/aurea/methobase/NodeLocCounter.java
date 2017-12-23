package com.aurea.methobase;

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
import com.github.javaparser.ast.type.VoidType;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeLocCounter {

    private static final List<Predicate<Node>> IS_INSIGNIFICANT_BLOCKSTMT_CONDITIONS = List.of(
            p -> p instanceof MethodDeclaration && !(((MethodDeclaration) p).getType() instanceof VoidType),
            p -> p instanceof TryStmt,
            p -> p instanceof ForStmt,
            p -> p instanceof IfStmt,
            p -> p instanceof WhileStmt,
            p -> p instanceof DoStmt
    );

    public long count(List<Node> nodes) {
        long count = countRecursively(nodes);
        count += nodes.stream().map(Node::getChildNodes).mapToLong(this::count).sum();
        return count;
    }

    private long countRecursively(List<Node> nodes) {
        long statements = significantStatements(nodes).count();
        long blockStatements = blockStatements(nodes).mapToLong(this::calculateBlockStmt).sum();

        final Map<Boolean, List<VariableDeclarator>> variablesByScope = variableDeclarators(nodes)
                .collect(Collectors.partitioningBy(this::isField));
        final List<VariableDeclarator> fieldDeclarations = variablesByScope.get(true);
        final List<VariableDeclarator> variableDeclarations = variablesByScope.get(false);
        long fields = fieldDeclarations.stream()
                .filter(this::isSignificantFieldDeclaration).count();
        long insignificantVariables = variableDeclarations.stream()
                .filter(this::isInsignficantVariableDeclaration).count();
        return statements + blockStatements + fields - insignificantVariables;
    }

    private Stream<Node> significantStatements(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof Statement &&
                        !(x instanceof BlockStmt) &&
                        !(x instanceof TryStmt));
    }

    private Stream<BlockStmt> blockStatements(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof BlockStmt)
                .map(BlockStmt.class::cast);
    }

    private Stream<VariableDeclarator> variableDeclarators(List<Node> nodes) {
        return nodes.stream()
                .filter(x -> x instanceof VariableDeclarator)
                .map(VariableDeclarator.class::cast);
    }

    private boolean isSignificantFieldDeclaration(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getInitializer().isPresent() && !isClassConstant(variableDeclarator);
    }

    private boolean isInsignficantVariableDeclaration(VariableDeclarator variableDeclarator) {
        return !variableDeclarator.getInitializer().isPresent() &&
                variableDeclarator.getParentNode()
                        .filter(VariableDeclarationExpr.class::isInstance)
                        .flatMap(Node::getParentNode)
                        .map(ExpressionStmt.class::isInstance).orElse(false);
    }

    private boolean isClassConstant(VariableDeclarator variableDeclarator) {
        return isFinalStaticField(variableDeclarator) && hasLiteralInitializer(variableDeclarator);
    }

    private boolean isField(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getParentNode()
                .map(FieldDeclaration.class::isInstance)
                .orElse(false);
    }

    private Boolean isFinalStaticField(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getParentNode()
                .filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast)
                .map(fd -> fd.isFinal() && fd.isStatic())
                .orElse(false);
    }

    private boolean hasLiteralInitializer(VariableDeclarator variableDeclarator) {
        return variableDeclarator.getInitializer()
                .map(LiteralExpr.class::isInstance)
                .orElse(false);
    }

    private long calculateBlockStmt(BlockStmt blockStmt) {
        return blockStmt.getParentNode()
                .map(parent -> IS_INSIGNIFICANT_BLOCKSTMT_CONDITIONS.stream()
                        .anyMatch(condition -> condition.test(parent)) ? 0L : 1L)
                .orElse(1L);
    }
}
