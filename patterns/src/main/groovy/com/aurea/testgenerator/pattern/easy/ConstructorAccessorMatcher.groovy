package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

import static com.aurea.testgenerator.ast.ASTNodeUtils.findChildOf
import static com.aurea.testgenerator.ast.ASTNodeUtils.findParentSubTypeOf
import static java.util.Optional.empty

@Component
class ConstructorAccessorMatcher extends AccessorMatcher {

    LiteralResolver resolver = new LiteralResolver()

    ConstructorAccessorMatcher(CoverageService coverageService, JavaParserFacade javaParserFacade) {
        super(coverageService, javaParserFacade)
    }

    @Override
    Optional<? extends AccessorMatch> createMatch(Unit unit, MethodDeclaration n, JavaParserFacade facade) {
        ReturnStmt returnStmt = findChildOf(ReturnStmt.class, n).get()
        Optional<NameExpr> fieldReferenceName = findChildOf(NameExpr.class, returnStmt)
        if (fieldReferenceName.isPresent()) {
            Optional<JavaParserFieldDeclaration> declarator = resolveDeclaration(fieldReferenceName.get(), facade, n)
            if (declarator.isPresent()) {
                JavaParserFieldDeclaration javaParserFieldDeclaration = declarator.get()
                VariableDeclarator variableDeclarator = javaParserFieldDeclaration.variableDeclarator.get()
                if (variableDeclarator.initializer.isPresent()) {
                    return empty()
                } else {
                    Optional<ConstructorFieldInitializer> initializer = tryFindConstructorInitialization(variableDeclarator, n)
                    return initializer.map { new ConstructorAccessorMatch(unit, n, it) }
                }
            }
        }
        return empty()
    }

    Optional<ConstructorFieldInitializer> tryFindConstructorInitialization(VariableDeclarator variableDeclarator, MethodDeclaration n) {
        TypeDeclaration type = findParentSubTypeOf(TypeDeclaration.class, n)
        List<ConstructorDeclaration> constructors = type.getNodesByType(ConstructorDeclaration.class)
        Optional<AssignExpr> firstAssignment = StreamEx.of(constructors)
                .flatMap { ASTNodeUtils.findFieldAssignmentsInGivenNodeByName(it, variableDeclarator.name) }
                .findFirst()
        return firstAssignment.map { deduceFieldInitializer(it) }
    }

    private ConstructorFieldInitializer deduceFieldInitializer(AssignExpr assignExpr) {
        ConstructorDeclaration cd = findParentSubTypeOf(ConstructorDeclaration.class, assignExpr)
        Optional<String> literalExpr = resolver.tryFindLiteralExpression(assignExpr.value)
        if (literalExpr.isPresent()) {
            return new ConstructorLiteralFieldInitializer(cd, literalExpr.get())
        } else {
            if (assignExpr.value instanceof NameExpr) {
                SimpleName referenceName = (assignExpr.value as NameExpr).name
                Parameter parameter = cd.parameters.find { it.name == referenceName }
                if (parameter != null) {
                    return new ConstructorParameterFieldInitializer(cd, parameter)
                }
            }
        }
        return null
    }

    private static Optional<JavaParserFieldDeclaration> resolveDeclaration(NameExpr fieldReference, JavaParserFacade facade, MethodDeclaration n) {
        SymbolReference<? extends ResolvedValueDeclaration> valueDeclaration = facade.solve(fieldReference)
        ResolvedValueDeclaration declaration
        if (valueDeclaration.isSolved()) {
            declaration = valueDeclaration.correspondingDeclaration
            if (declaration.isField()) {
                return Optional.ofNullable(declaration as JavaParserFieldDeclaration)
            }
        }
        return empty()
    }
}