package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestNodeVariable
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorPatterns
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class ArgumentAssignmentGenerator implements PatternToTest {

    JavaParserFacade solver
    FieldAssignments fieldAssignments
    ValueFactory valueFactory

    @Autowired
    ArgumentAssignmentGenerator(FieldAssignments fieldAssignments, JavaParserFacade solver, ValueFactory valueFactory) {
        this.fieldAssignments = fieldAssignments
        this.solver = solver
        this.valueFactory = valueFactory
    }

    @Override
    void accept(PatternMatch patternMatch, TestUnit testUnit) {
        if (patternMatch.pattern != ConstructorPatterns.HAS_ARGUMENT_ASSIGNMENTS) {
            return
        }

        ConstructorDeclaration cd = patternMatch.match.asConstructorDeclaration()
        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)

        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = fieldAssignments.findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> onlyArgumentAssignExprs = onlyLastAssignExprs.findAll {
            it.value.nameExpr && cd.isNameOfArgument(it.value.asNameExpr().name)
        }
        AssertionBuilder builder = AssertionBuilder.buildFor(testUnit).softly(onlyArgumentAssignExprs.size() > 1)
        for (AssignExpr assignExpr : onlyArgumentAssignExprs) {
            FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
            Optional<ResolvedFieldDeclaration> maybeField = fieldAccessExpr.findField(solver)
            ResolvedType fieldType = maybeField.get().getType()
            Optional<Expression> maybeFieldAccessExpression = fieldAssignments.buildFieldAccessExpression(assignExpr, scope)
            Expression expected = assignExpr.value
            maybeFieldAccessExpression.ifPresent { fieldAccessExpression ->
                builder.with(fieldType, fieldAccessExpression, expected)
            }
        }
        List<Statement> assertions = builder.build()
        if (!assertions.empty) {
            List<TestNodeVariable> variables = StreamEx.of(cd.parameters).map { p ->
                valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                    new RuntimeException("Failed to build variable for parameter $p of $cd")
                }
            }.toList()
            variables.each {
                testUnit.addDependency it.dependency
            }
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.expr)}

            Map<SimpleName, TestNodeExpression> variableExpressionsByNames = StreamEx.of(variables)
                                                                                     .toMap(
                    { it.expr.getVariable(0).name },
                    {
                        new TestNodeExpression(expr: new NameExpr(it.expr.getVariable(0).name))
                    })

            Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory)
                    .usingForParameters(variableExpressionsByNames)
                    .build(cd)
            constructorCall.ifPresent { constructCallExpr ->
                String assignsConstantsCode = """
            @Test
            public void test_${cd.nameAsString}_AssignsArgumentsToFields() throws Exception {
                ${variableStatements.join(System.lineSeparator())}

                ${cd.nameAsString} $instanceName = ${constructCallExpr.expr};
                
                ${assertions.join(System.lineSeparator())}
            }
            """
                MethodDeclaration assignsArguments = JavaParser.parseBodyDeclaration(assignsConstantsCode)
                                                               .asMethodDeclaration()
                testUnit.addImport Imports.JUNIT_TEST
                testUnit.addDependency constructCallExpr.dependency
                testUnit.addTest assignsArguments
            }
        }
    }
}
