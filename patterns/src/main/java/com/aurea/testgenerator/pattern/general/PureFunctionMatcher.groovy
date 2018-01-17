package com.aurea.testgenerator.pattern.general

import com.aurea.testgenerator.pattern.AbstractSubjectMethodMatcher
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import one.util.streamex.StreamEx

import java.util.function.BiPredicate

import static java.lang.System.lineSeparator

/*
 * https://www.sitepoint.com/functional-programming-pure-functions/
 */

class PureFunctionMatcher extends AbstractSubjectMethodMatcher {

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
                (MethodCallExpr)            : IMPURE,
                (MethodReferenceExpr)       : IMPURE,
                (SuperExpr)                 : IMPURE,
                (AssignExpr)                : new AssignExprPureFunctionPredicate(), // Handle assignments for each concrete expr
                (ArrayAccessExpr)           : new ArrayAccessExprPureFunctionPredicate(),
                (FieldAccessExpr)           : new FieldAccessExprPureFunctionPredicate(),
                (ThisExpr)                  : new ThisExprPureFunctionPredicate(),
                (UnaryExpr)                 : new UnaryExprPureFunctionPredicate()

        ]

    }

    @Override
    Optional<? extends PatternMatch> matchMethod(Unit unit, MethodDeclaration n) {
        List<Expression> expressions = n.getNodesByType(Expression)
        logger.debug "XML: ${xmlConverter.toXmlString(n)}"
        MethodContext context = MethodContext.buildForMethod(n)

        Map<Boolean, List<Expression>> partitionedExpressions = StreamEx.of(expressions).partitioningBy { Expression e ->
            BiPredicate<? extends Expression, MethodContext> tester = PURITY_FUNCTIONS.get(e.getClass())
            tester.test(e, context)
        }
        List<Expression> pureExpressions = partitionedExpressions[true]
        List<Expression> impureExpressions = partitionedExpressions[false]

        logger.debug("Pure expressions: " + lineSeparator() + pureExpressions.join(lineSeparator()))
        logger.debug("Impure expressions: " + lineSeparator() + impureExpressions.join(lineSeparator()))

        boolean isPure = impureExpressions.empty
        if (isPure) {
            return Optional.of(new PureFunctionMatch(unit, n.nameAsString))
        }
        Optional.empty()
    }
}
