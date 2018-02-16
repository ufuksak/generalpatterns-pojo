package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.FieldAccessBuilder
import com.aurea.testgenerator.ast.FieldAccessResult

import com.aurea.testgenerator.ast.FieldAssignments
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class ConstructorFieldLiteralAssignmentsGenerator extends AbstractConstructorTestGenerator {

    JavaParserFacade solver
    FieldResolver fieldResolver
    ValueFactory valueFactory

    @Autowired
    ConstructorFieldLiteralAssignmentsGenerator(JavaParserFacade solver, ValueFactory valueFactory) {
        this.solver = solver
        this.fieldResolver = new FieldResolver(solver)
        this.valueFactory = valueFactory
    }

    @Override
    protected TestGeneratorResult generate(ConstructorDeclaration cd, Unit unitUnderTest) {
        TestGeneratorResult result = new TestGeneratorResult()
        String instanceName = cd.nameAsString.uncapitalize()
        Expression scope = new NameExpr(instanceName)
        FieldAccessBuilder fieldAccessBuilder = new FieldAccessBuilder(scope)

        ArrayList<AssignExpr> literalAssignExpressions = findLiteralAssignmentExpressions(cd)
        AssertionBuilder assertionBuilder = new AssertionBuilder().softly(literalAssignExpressions.size() > 1)
        literalAssignExpressions.each {
            addAssertion(it, fieldAccessBuilder, assertionBuilder, result)
        }
        List<DependableNode<Statement>> assertions = assertionBuilder.build()
        if (!assertions.empty) {
            DependableNode<MethodDeclaration> assignConstants = new DependableNode<>()
            TestNodeMerger.appendDependencies(assignConstants, assertions)
            Optional<DependableNode<ObjectCreationExpr>> constructorCall = new InvocationBuilder(valueFactory).build(cd)
            constructorCall.ifPresent { constructCallExpr ->
                TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
                TestNodeMerger.appendDependencies(assignConstants, constructCallExpr)
                String testName = testMethodNomenclature.requestTestMethodName(getType(), cd)
                String assignsConstantsCode = """
                    @Test
                    public void ${testName}() throws Exception {
                        ${constructCallExpr.node.type} $instanceName = ${constructCallExpr.node};
                        
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

    private static Collection<AssignExpr> findLiteralAssignmentExpressions(ConstructorDeclaration cd) {
        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        Collection<AssignExpr> onlyLastAssignExprs = FieldAssignments.findLastAssignExpressionsByField(assignExprs)
        onlyLastAssignExprs.findAll { it.value.literalExpr }
    }

    private void addAssertion(AssignExpr assignExpr,
                              FieldAccessBuilder fieldAccessBuilder,
                              AssertionBuilder assertionBuilder,
                              TestGeneratorResult result) {
        FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
        Optional<ResolvedFieldDeclaration> maybeField = fieldResolver.resolve(fieldAccessExpr)
        if (maybeField.present) {
            ResolvedFieldDeclaration field = maybeField.get()
            FieldAccessResult fieldAccessResult = fieldAccessBuilder.build(field)
            if (fieldAccessResult.type == FieldAccessResult.Type.SUCCESS) {
                ResolvedType fieldType = field.getType()
                Expression expected = assignExpr.value
                assertionBuilder.with(fieldType, fieldAccessResult.expression, expected)
            } else if (fieldAccessResult.type == FieldAccessResult.Type.FAILED) {
                result.errors << fieldAccessResult.error
            }
        } else {
            result.errors << TestGeneratorError.unsolved(fieldAccessExpr)
        }
    }

    @Override
    protected TestType getType() {
        ConstructorTypes.CONSTRUCTOR_FIELD_LITERAL_ASSIGNMENTS
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        Callability.isCallableFromTests(cd) && ASTNodeUtils.parents(cd, TypeDeclaration).noneMatch { it.enumDeclaration }
    }
}
