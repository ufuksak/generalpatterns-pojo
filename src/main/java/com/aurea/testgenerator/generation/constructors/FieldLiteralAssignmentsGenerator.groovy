package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.generation.PatternToTest
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.pattern.PatternMatch
import com.aurea.testgenerator.pattern.general.constructors.ConstructorPatterns
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
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

        List<AssignExpr> assignExprs = cd.body.findAll(AssignExpr)
        for (AssignExpr assignExpr: assignExprs) {
            SimpleName fieldName = assignExpr.target.asFieldAccessExpr().name
            try {
                SymbolReference<? extends ResolvedValueDeclaration> fieldReference = solver.solve(fieldName)
                if (fieldReference.solved) {
                    JavaParserFieldDeclaration fieldDeclaration = fieldReference.correspondingDeclaration.asField()
                    FieldDeclaration field = fieldDeclaration.wrappedNode
                    Type type = field.getType()
                } else {
                    log.error "Failed to solve $fieldName in $testUnit.unitUnderTest.fullName"
                    return
                }
            } catch (UnsolvedSymbolException use) {
                log.error "Failed to solve $fieldName in $testUnit.unitUnderTest.fullName", use
                return
            }
        }

//        Optional<TestNodeExpression> constructorCall = new InvocationBuilder(new RandomValueFactory()).build(cd)
//        constructorCall.ifPresent { constructCallExpr ->
//            MethodDeclaration typeIsInstantiableTest = JavaParser.parseBodyDeclaration("""
//            @Test
//            public void test_${cd.nameAsString}_IsInstantiable() throws Exception {
//                ${constructCallExpr.expr};
//            }
//            """).asMethodDeclaration()
//            testUnit.addImport Imports.JUNIT_TEST
//            testUnit.addTest typeIsInstantiableTest
//        }
    }
}
