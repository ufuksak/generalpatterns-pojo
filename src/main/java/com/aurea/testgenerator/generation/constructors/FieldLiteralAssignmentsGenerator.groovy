package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.*
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import groovy.util.logging.Log4j2
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
    protected TestGeneratorResult generate(ConstructorDeclaration cd, Unit unitUnderTest) {
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
            TestNodeMethod assignConstants = new TestNodeMethod()
            TestNodeMerger.appendDependencies(assignConstants, assertions)
            Optional<TestNodeExpression> constructorCall = new InvocationBuilder(valueFactory).build(cd)
            constructorCall.ifPresent { constructCallExpr ->
                TestMethodNomenclature testMethodNomenclature = namerFactory.getTestMethodNomenclature(unitUnderTest.javaClass)
                TestNodeMerger.appendDependencies(assignConstants, constructCallExpr)
                String testName = testMethodNomenclature.requestTestMethodName(getType(), cd)
                String assignsConstantsCode = """
                    @Test
                    public void ${testName}() throws Exception {
                        ${cd.nameAsString} $instanceName = ${constructCallExpr.node};
                        
                        ${assertions.collect { it.node }.join(System.lineSeparator())}
                    }
                """

                assignConstants.node = JavaParser.parseBodyDeclaration(assignsConstantsCode)
                                                   .asMethodDeclaration()
                assignConstants.dependency.imports << Imports.JUNIT_TEST
                result.tests = [assignConstants]
            }
        }
        result
    }

    @Override
    protected TestType getType() {
        ConstructorTypes.FIELD_LITERAL_ASSIGNMENTS
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        Callability.isCallableFromTests(cd) && ASTNodeUtils.parents(cd, TypeDeclaration).noneMatch { it.enumDeclaration }
    }
}
