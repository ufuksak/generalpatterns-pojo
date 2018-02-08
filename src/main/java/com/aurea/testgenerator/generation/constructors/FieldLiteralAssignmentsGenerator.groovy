package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.*
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class FieldLiteralAssignmentsGenerator extends AbstractConstructorTestGenerator {

    FieldAssignments fieldAssignments
    JavaParserFacade solver
    ValueFactory valueFactory

    @Autowired
    FieldLiteralAssignmentsGenerator(FieldAssignments fieldAssignments, JavaParserFacade solver, ValueFactory valueFactory) {
        this.fieldAssignments = fieldAssignments
        this.solver = solver
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(ConstructorDeclaration cd) {
        TestGeneratorResult result = new TestGeneratorResult()
        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)

        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = fieldAssignments.findLastAssignExpressionsByField(assignExprs)
        Collection<AssignExpr> onlyLiteralAssignExprs = onlyLastAssignExprs.findAll { it.value.literalExpr }
        AssertionBuilder builder = new AssertionBuilder().softly(onlyLiteralAssignExprs.size() > 1)
        for (AssignExpr assignExpr : onlyLiteralAssignExprs) {
            FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
            try {
                Expression expected = assignExpr.value
                Optional<ResolvedFieldDeclaration> maybeField = fieldAccessExpr.findField(solver)
                if (maybeField.present) {
                    Optional<Expression> maybeFieldAccessExpression = fieldAssignments.buildFieldAccessExpression(assignExpr, scope)
                    maybeFieldAccessExpression.ifPresent { fieldAccessExpression ->
                        builder.with(maybeField.get().getType(), fieldAccessExpression, expected)
                    }
                } else {
                    result.errors << new TestGeneratorError("Failed to solve field access $fieldAccessExpr")
                }
            } catch (UnsolvedSymbolException use) {
                result.errors << new TestGeneratorError("Failed to solve field access $fieldAccessExpr")
            }
        }
        List<TestNodeStatement> assertions = builder.build()
        if (!assertions.empty) {
            Set<ImportDeclaration> imports = StreamEx.of(assertions).flatMap { it.dependency.imports.stream() }.toSet()
            Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory).build(cd)
            constructorCall.ifPresent { constructCallExpr ->
                String assignsConstantsCode = """
            @Test
            public void test_${cd.nameAsString}_AssignsConstantsToFields() throws Exception {
                ${cd.nameAsString} $instanceName = ${constructCallExpr.expr};
                
                ${assertions.collect { it.stmt }.join(System.lineSeparator())}
            }
            """
                MethodDeclaration assignsConstants = JavaParser.parseBodyDeclaration(assignsConstantsCode)
                                                               .asMethodDeclaration()
                imports.addAll constructCallExpr.dependency.imports
                imports << Imports.JUNIT_TEST
                result.tests = [
                        new TestNodeMethod(
                                dependency: new TestDependency(imports: imports),
                                md: assignsConstants
                        )
                ]
            }
        }
        result
    }

    @Override
    protected TestType getType() {
        ConstructorTypes.FIELD_LITERAL_ASSIGNMENTS
    }
}
