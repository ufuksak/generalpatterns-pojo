package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorErrorContainer
import com.aurea.testgenerator.generation.UnsolvedException
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.patterns.pojos.PojoFieldFinder
import com.aurea.testgenerator.value.Types
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2

@Log4j2
class SetterAssertionBuilder {

    FieldAccessBuilder fieldAccessBuilder
    FieldResolver fieldResolver
    TestGeneratorErrorContainer errorContainer
    AssertionBuilder assertionBuilder

    SetterAssertionBuilder(FieldAccessBuilder fieldAccessBuilder,
                           JavaParserFacade solver,
                           TestGeneratorErrorContainer errorContainer,
                           AssertionBuilder assertionBuilder) {
        this.fieldAccessBuilder = fieldAccessBuilder
        this.fieldResolver = new FieldResolver(solver)
        this.errorContainer = errorContainer
        this.assertionBuilder = assertionBuilder
    }

    SetterAssertionBuilder(Expression scope,
                           JavaParserFacade solver,
                           TestGeneratorErrorContainer errorContainer,
                           AssertionBuilder assertionBuilder) {
        this(new FieldAccessBuilder(scope), solver, errorContainer, assertionBuilder)
    }

    SetterAssertionBuilder withAssertion(MethodCallExpr setterCall) {
        Types.tryResolve(setterCall).ifPresent { resolvedMethod ->
            PojoFieldFinder fieldFinder = PojoFieldFinder.fromSetter(resolvedMethod)
            Optional<ResolvedFieldDeclaration> maybeField = fieldFinder.tryToFindField()
            try {
                ResolvedFieldDeclaration field = maybeField.orElseThrow {
                    throw new UnsolvedException(setterCall)
                }
                FieldAccessResult fieldAccessResult = fieldAccessBuilder.build(field)
                if (fieldAccessResult.success) {
                    ResolvedType fieldType = field.getType()
                    Expression expected = setterCall.arguments.first()
                    assertionBuilder.with(fieldType, fieldAccessResult.expression, expected)
                } else if (fieldAccessResult.failed) {
                    errorContainer.errors << fieldAccessResult.error
                } else {
                    log.debug "Field $field is not accessible from tests, skipping assertion"
                }
            } catch (UnsolvedException ue) {
                errorContainer.errors << TestGeneratorError.unsolved(ue.unsolvedNode)
            }
        }
        this
    }

    SetterAssertionBuilder withAssertions(Collection<MethodCallExpr> setterCalls) {
        assertionBuilder.softly(setterCalls.size() > 1)
        setterCalls.each { withAssertion(it) }
        this
    }

    List<DependableNode<Statement>> build() {
        assertionBuilder.build()
    }
}
