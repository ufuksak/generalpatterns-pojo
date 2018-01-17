package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.coverage.CoverageService
import com.github.javaparser.ast.body.InitializerDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr
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
class ClassConstantAccessorMatcher extends AccessorMatcher {

    LiteralResolver resolver = new LiteralResolver()

    ClassConstantAccessorMatcher() {
        super()
    }

    ClassConstantAccessorMatcher(CoverageService coverageService, JavaParserFacade javaParserFacade) {
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
                Optional<String> literalExpr
                if (variableDeclarator.initializer.isPresent()) {
                    literalExpr = resolver.tryFindLiteralExpression(variableDeclarator.initializer.get())
                } else {
                    if (!javaParserFieldDeclaration.isStatic()) {
                        return empty()
                    }
                    literalExpr = tryFindLiteralExpressionInStaticBlocks(variableDeclarator, n)
                }
                return literalExpr.map { new ClassConstantAccessorMatch(unit, n, it) }
            }
        }
        return empty()
    }

    Optional<String> tryFindLiteralExpressionInStaticBlocks(VariableDeclarator variableDeclarator, MethodDeclaration n) {
        TypeDeclaration type = findParentSubTypeOf(TypeDeclaration.class, n)
        List<InitializerDeclaration> staticBlocks = StreamEx.of(type.getNodesByType(InitializerDeclaration.class))
                .filter { it.isStatic() }
                .toList()
        List<AssignExpr> assignExprs = StreamEx.of(staticBlocks)
                .flatMap{ASTNodeUtils.findFieldAssignmentsInGivenNodeByName(it, variableDeclarator.name)}
                .toList()
        if (!assignExprs.isEmpty()) {
            AssignExpr assignExpr = assignExprs.last()
            return resolver.tryFindLiteralExpression(assignExpr.value)
        }
        return empty()
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