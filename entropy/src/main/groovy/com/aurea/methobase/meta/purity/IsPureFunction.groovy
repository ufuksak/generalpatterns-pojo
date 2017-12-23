package com.aurea.methobase.meta.purity

import com.aurea.testgenerator.symbolsolver.SymbolSolver
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

import java.util.function.BiPredicate
import java.util.function.Predicate

/*
 * https://www.sitepoint.com/functional-programming-pure-functions/
 */
@Log4j2
class IsPureFunction implements Predicate<MethodDeclaration> {

    static final Map<Class<? extends Expression>, BiPredicate<? extends Expression, MethodContext>> PURITY_FUNCTIONS

    static {
        def PURE = { expr, context -> true }
        def IMPURE = { expr, context -> false }
        PURITY_FUNCTIONS = [
                (AnnotationExpr)            : PURE,
                (BinaryExpr)                : PURE,
                (BooleanLiteralExpr)        : PURE,
                (ClassExpr)                 : PURE,
                (CharLiteralExpr)           : PURE,
                (CastExpr)                  : PURE,
                (ConditionalExpr)           : PURE,
                (DoubleLiteralExpr)         : PURE,
                (EnclosedExpr)              : PURE,
                (InstanceOfExpr)            : PURE,
                (LambdaExpr)                : PURE,
                (LiteralExpr)               : PURE,
                (LiteralStringValueExpr)    : PURE,
                (IntegerLiteralExpr)        : PURE,
                (LongLiteralExpr)           : PURE,
                (MarkerAnnotationExpr)      : PURE,
                (NameExpr)                  : PURE,
                (NormalAnnotationExpr)      : PURE,
                (NullLiteralExpr)           : PURE,
                (SingleMemberAnnotationExpr): PURE,
                (StringLiteralExpr)         : PURE,
                (VariableDeclarationExpr)   : PURE,
                (ArrayCreationExpr)         : PURE,
                (ObjectCreationExpr)        : PURE,
                (ArrayInitializerExpr)      : PURE,
                (TypeExpr)                  : PURE,
                (MethodReferenceExpr)       : PURE,
                (MethodCallExpr)            : IMPURE,
                (SuperExpr)                 : IMPURE,
                (AssignExpr)                : new AssignExprPureFunctionPredicate(), // Handle assignments for each concrete expr
                (ArrayAccessExpr)           : new ArrayAccessExprPureFunctionPredicate(),
                (FieldAccessExpr)           : new FieldAccessExprPureFunctionPredicate(),
                (ThisExpr)                  : new ThisExprPureFunctionPredicate(),
                (UnaryExpr)                 : new UnaryExprPureFunctionPredicate()

        ]
    }

    SymbolSolver solver

    IsPureFunction(SymbolSolver solver) {
        this.solver = solver
    }

    @Override
    boolean test(MethodDeclaration n) {
        List<Expression> expressions = n.findAll(Expression)
        MethodContext context = MethodContext.buildForMethod(n)

        Map<Boolean, List<Expression>> partitionedExpressions = StreamEx.of(expressions).partitioningBy { Expression e ->
            BiPredicate<? extends Expression, MethodContext> tester = PURITY_FUNCTIONS.get(e.getClass())
            tester.test(e, context)
        }
        List<Expression> pureExpressions = partitionedExpressions[true]
        List<Expression> impureExpressions = partitionedExpressions[false]

        log.debug("Pure expressions: " + System.lineSeparator() + pureExpressions.join(System.lineSeparator()))
        log.debug("Impure expressions: " + System.lineSeparator() + impureExpressions.join(System.lineSeparator()))

        impureExpressions.empty
    }
}
