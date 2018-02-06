package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.TestNodeExpression
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.generation.source.AssertionBuilder
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorPatterns
import com.aurea.testgenerator.value.random.RandomValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j2
class FieldLiteralAssignmentsGenerator implements PatternToTest {

    JavaParserFacade solver

    @Autowired
    FieldLiteralAssignmentsGenerator(JavaParserFacade solver) {
        this.solver = solver
    }

    @Override
    void accept(PatternMatch patternMatch, TestUnit testUnit) {
        if (patternMatch.pattern != ConstructorPatterns.FIELD_LITERAL_ASSIGNMENTS) {
            return
        }

        ConstructorDeclaration cd = patternMatch.match.asConstructorDeclaration()
        String instanceName = cd.nameAsString.uncapitalize()

        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        AssertionBuilder builder = AssertionBuilder.buildFor(testUnit).softly(assignExprs.size() > 1)
        for (AssignExpr assignExpr : assignExprs) {
            if (!assignExpr.value.literalExpr) {
                log.error "FieldLiteralAssignmentsGenerator expects assign expression to be literal but got ${assignExpr.value}"
                return
            }
            if (!assignExpr.target.fieldAccessExpr) {
                log.error "FieldLiteralAssignmentsGenerator expects target of assign expression to be a field access, but got ${assignExpr.target}"
                return
            }
            Expression expected = assignExpr.value
            FieldAccessExpr fieldAccessExpr = assignExpr.target.asFieldAccessExpr()
            Optional<ResolvedFieldDeclaration> maybeField = fieldAccessExpr.findField()
            maybeField.map { field ->

            }
            Optional<Expression> maybeActual = buildAccessToField(maybeField, testUnit, cd, instanceName)
            maybeActual.ifPresent { actual ->
                Optional<ResolvedType> maybeType = solveFieldType(assignExpr.target.asFieldAccessExpr().name, testUnit)
                if (maybeType.present) {
                    builder.with(maybeType.get(), actual, expected)
                }
            }
        }
        List<Statement> assertions = builder.build()
        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(new RandomValueFactory()).build(cd)
        constructorCall.ifPresent { constructCallExpr ->
            String assignsConstantsCode = """
            @Test
            public void test_${cd.nameAsString}_AssignsConstantsToFields() throws Exception {
                ${cd.nameAsString} $instanceName = ${constructCallExpr.expr};
                
                ${assertions.join(System.lineSeparator())}
            }
            """
            MethodDeclaration assignsConstants = JavaParser.parseBodyDeclaration(assignsConstantsCode)
                                                           .asMethodDeclaration()
            testUnit.addImport Imports.JUNIT_TEST
            testUnit.addTest assignsConstants
        }
    }

    Optional<ResolvedType> solveFieldType(SimpleName fieldName, TestUnit testUnit) {
        try {
            SymbolReference<? extends ResolvedValueDeclaration> fieldReference = solver.solve(fieldName)
            if (fieldReference.solved) {
                ResolvedFieldDeclaration fieldDeclaration = fieldReference.correspondingDeclaration.asField()
                return Optional.of(fieldDeclaration.getType())
            } else {
                log.error "Failed to solve $fieldName in $testUnit.unitUnderTest.fullName"
            }
        } catch (UnsolvedSymbolException use) {
            log.error "Failed to solve $fieldName in $testUnit.unitUnderTest.fullName", use
        }
        Optional.empty()
    }

    Optional<Expression> buildAccessToField(ResolvedFieldDeclaration rfd, String instanceName) {
        if (rfd.accessSpecifier() != AccessSpecifier.PRIVATE && rfd.static) {
            return Optional.ofNullable(new FieldAccessExpr(
                    new NameExpr(rfd.declaringType().name), rfd.name
            ))
        } else if (rfd.accessSpecifier() != AccessSpecifier.PRIVATE) {
            return Optional.ofNullable(new FieldAccessExpr(
                    new NameExpr(new SimpleName(instanceName)), rfd.name))
        } else {
            return Optional.empty()
        }
    }
}
