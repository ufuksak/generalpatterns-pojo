package com.aurea.methobase

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*

class CognitiveComplexityNodeCalculator {

    private static final Map<Class<? extends Node>, Closure<Integer>> COMPLEXITY_CALCULATORS
    private static final Map<Class<? extends Expression>, Closure<Integer>> EXPRESSION_CALCULATORS

    static {
        COMPLEXITY_CALCULATORS = new HashMap<>()
        COMPLEXITY_CALCULATORS[BinaryExpr] = { BinaryExpr expr, int nesting ->
            1 + calculateExpression(expr, nesting)
        }
        COMPLEXITY_CALCULATORS[IfStmt] = { IfStmt ifStmt, int nesting ->
            calculateIfStmt(ifStmt, nesting, true)
        }
        COMPLEXITY_CALCULATORS[ForStmt] = { ForStmt forStmt, int nesting ->
            nesting + 1 +
                    forStmt.compare.map { complexity(it, nesting) }.orElse(0) +
                    complexity(forStmt.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[ForeachStmt] = { ForeachStmt foreachStmt, int nesting ->
            nesting + 1 + complexity(foreachStmt.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[WhileStmt] = { WhileStmt whileStmt, int nesting ->
            nesting + 1 +
                    complexity(whileStmt.condition, nesting) +
                    complexity(whileStmt.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[DoStmt] = { DoStmt doStmt, int nesting ->
            nesting + 1 +
                    complexity(doStmt.condition, nesting) +
                    complexity(doStmt.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[SwitchStmt] = { SwitchStmt switchStmt, int nesting ->
            nesting + 1 +
                    complexity(switchStmt.selector, nesting) +
                    switchStmt.entries.stream().mapToInt { complexity(it, nesting + 1) }.sum()
        }
        COMPLEXITY_CALCULATORS[CatchClause] = { CatchClause catchClause, int nesting ->
            nesting + 1 + complexity(catchClause.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[SynchronizedStmt] = { SynchronizedStmt synchronizedStmt, int nesting ->
            nesting + 1 + complexity(synchronizedStmt.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[LambdaExpr] = { LambdaExpr expr, int nesting ->
            complexity(expr.body, nesting + 1)
        }
        COMPLEXITY_CALCULATORS[ConditionalExpr] = { ConditionalExpr expr, int nesting ->
            1 + expr.childNodes.stream().mapToInt { complexity(it, nesting + 1) }.sum()
        }
        COMPLEXITY_CALCULATORS[BreakStmt] = { BreakStmt breakStmt, int nesting ->
            breakStmt.label.present ? 1 : 0
        }
        COMPLEXITY_CALCULATORS[ContinueStmt] = { ContinueStmt continueStmt, int nesting ->
            continueStmt.label.present ? 1 : 0
        }
        COMPLEXITY_CALCULATORS[MethodCallExpr] = { MethodCallExpr methodCallExpr, int nesting ->
            int recursiveScore = methodCallExpr.getAncestorOfType(MethodDeclaration).map {
                it.nameAsString == methodCallExpr.nameAsString ? 1 : 0
            }.orElse(0)
            int childScore = methodCallExpr.arguments.stream().mapToInt{complexity(it, nesting)}.sum()
            recursiveScore + childScore
        }

        EXPRESSION_CALCULATORS = new HashMap<>()
        EXPRESSION_CALCULATORS[BinaryExpr] = { BinaryExpr binaryExpr, int nesting, BinaryExpr.Operator operator ->
            int score = binaryExpr.operator == operator ? 0 : 1
            score + calculateExpression(binaryExpr, nesting)
        }
        EXPRESSION_CALCULATORS[EnclosedExpr] = { EnclosedExpr enclosedExpr, int nesting, BinaryExpr.Operator operator ->
            Optional.ofNullable(enclosedExpr.inner).map { calculateExpressionComplexity(it, nesting, operator) }.orElse(0)
        }
        EXPRESSION_CALCULATORS[MethodCallExpr] = { MethodCallExpr methodCallExpr, int nesting, BinaryExpr.Operator operator ->
            int recursiveScore = methodCallExpr.getAncestorOfType(MethodDeclaration).map {
                it.nameAsString == methodCallExpr.nameAsString ? 1 : 0
            }.orElse(0)
            int childScore = methodCallExpr.arguments.stream().mapToInt{complexity(it, nesting)}.sum()
            recursiveScore + childScore
        }
        EXPRESSION_CALCULATORS[InstanceOfExpr] = { InstanceOfExpr instanceOfExpr, int nesting, BinaryExpr.Operator operator ->
            1 + calculateExpressionComplexity(instanceOfExpr.expression, nesting, operator)}
        EXPRESSION_CALCULATORS[UnaryExpr] = { UnaryExpr unaryExpr, int nesting, BinaryExpr.Operator operator ->
            1 + calculateExpressionComplexity(unaryExpr.expression, nesting, operator)
        }
    }

    static int visit(Node node) {
        complexity(node, 0)
    }

    private static int complexity(Node node, int nesting) {
        COMPLEXITY_CALCULATORS.getOrDefault(node.class, { Node noIncrementNode, int level ->
            noIncrementNode.childNodes.stream().mapToInt { complexity(it, level) }.sum()
        }).call(node, nesting)
    }

    private static int calculateIfStmt(IfStmt stmt, int nesting, boolean incrementNesting) {
        int statementScore = 1 + (incrementNesting ? nesting : 0)
        int conditionScore = complexity(stmt.condition, nesting)
        int blockScore = complexity(stmt.thenStmt, nesting + 1)
        int elseBlockScore = stmt.elseStmt.map { Statement elseStatement ->
            if (elseStatement instanceof IfStmt) {
                return calculateIfStmt(elseStatement as IfStmt, nesting, false)
            }
            return 1 + complexity(elseStatement, nesting)
        }.orElse(0)
        return statementScore + conditionScore + blockScore + elseBlockScore
    }

    private static int calculateExpression(BinaryExpr expr, int nesting) {
        calculateExpressionComplexity(expr.left, nesting, expr.operator) +
                calculateExpressionComplexity(expr.right, nesting, expr.operator)
    }

    private static int calculateExpressionComplexity(Expression expr, int nesting, BinaryExpr.Operator operator) {
        EXPRESSION_CALCULATORS.getOrDefault(expr.class, { Expression e, int n, BinaryExpr.Operator op -> 0 })
                              .call(expr, nesting, operator)
    }
}
