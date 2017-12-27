package com.aurea.methobase.meta.purity

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

import java.util.function.BiPredicate
import java.util.function.Predicate

/*
 * https://www.sitepoint.com/functional-programming-pure-functions/
 */

@Log4j2
class IsPureFunction implements Predicate<MethodDeclaration> {

    static final Map<Class<? extends Expression>, BiPredicate<? extends Expression, JavaParserFacade>> PURITY_FUNCTIONS

    static {
        def PURE = new BiPredicate<? extends Expression, JavaParserFacade>() {
            @Override
            boolean test(Expression o, JavaParserFacade javaParserFacade) {
                true
            }
        }
        def IMPURE = new BiPredicate<? extends Expression, JavaParserFacade>() {
            @Override
            boolean test(Expression o, JavaParserFacade javaParserFacade) {
                false
            }
        }
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
                (NameExpr)                  : new NameExprPureFunctionPredicate(),
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

    JavaParserFacade solver

    IsPureFunction(JavaParserFacade solver) {
        this.solver = solver
    }

    @Override
    boolean test(MethodDeclaration n) {
        boolean pure = StreamEx.of(n.findAll(Expression)).allMatch { isPure it }
        log.debug(n.findCompilationUnit()
                   .flatMap { it.storage }
                   .map { "${it.path}::${n.nameAsString} is ${pure ? "pure" : "impure"}" }
                   .orElse("${n.nameAsString} is ${pure ? "pure" : "impure"}"))
        pure
    }

    boolean isPure(Expression expr) {
        boolean pure = PURITY_FUNCTIONS.get(expr.class).test(expr, solver)
        log.trace "${expr.class.simpleName}: ${expr} is ${pure ? "pure" : "impure"}"
        pure
    }
}
