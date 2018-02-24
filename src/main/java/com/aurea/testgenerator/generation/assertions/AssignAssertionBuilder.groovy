package com.aurea.testgenerator.generation.assertions

import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorErrorContainer
import com.aurea.testgenerator.generation.UnsolvedException
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.value.Types
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2

@Log4j2
class AssignAssertionBuilder {

    FieldAccessBuilder fieldAccessBuilder
    FieldResolver fieldResolver
    TestGeneratorErrorContainer errorContainer
    AssertionBuilder assertionBuilder

    AssignAssertionBuilder(FieldAccessBuilder fieldAccessBuilder,
                           JavaParserFacade solver,
                           TestGeneratorErrorContainer errorContainer,
                           AssertionBuilder assertionBuilder) {
        this.fieldAccessBuilder = fieldAccessBuilder
        this.fieldResolver = new FieldResolver(solver)
        this.errorContainer = errorContainer
        this.assertionBuilder = assertionBuilder
    }

    AssignAssertionBuilder(Expression scope,
                           JavaParserFacade solver,
                           TestGeneratorErrorContainer errorContainer,
                           AssertionBuilder assertionBuilder) {
        this(new FieldAccessBuilder(scope), solver, errorContainer, assertionBuilder)
    }

    AssignAssertionBuilder withAssertion(AssignExpr assignExpr) {
        FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
        Optional<ResolvedFieldDeclaration> maybeField = fieldResolver.resolve(fieldAccessExpr.name)
        if (maybeField.present) {
            ResolvedFieldDeclaration field = maybeField.get()
            FieldAccessResult fieldAccessResult = fieldAccessBuilder.build(field)
            if (fieldAccessResult.success) {
                Optional<ResolvedType> fieldType = Types.tryGetType(field)
                fieldType.ifPresent {
                    Expression expected = assignExpr.value
                    assertionBuilder.with(it, fieldAccessResult.expression, expected)
                }
            } else if (fieldAccessResult.failed) {
                errorContainer.errors << fieldAccessResult.error
            } else {
                log.debug "Field $field is not accessible from tests, skipping assertion"
            }
        } else {
            errorContainer.errors << TestGeneratorError.unsolved(fieldAccessExpr)
        }
        this
    }

    AssignAssertionBuilder withAssertions(Collection<AssignExpr> assignExprs) {
        assertionBuilder.softly(assignExprs.size() > 1)
        assignExprs.each { withAssertion(it)}
        this
    }

    List<DependableNode<Statement>> build() {
        assertionBuilder.build()
    }
}
