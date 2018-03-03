package com.aurea.testgenerator.generation.patterns.singleton

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.InitializerDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference

import static com.aurea.testgenerator.value.Resolution.trySolve
import static com.aurea.testgenerator.value.Resolution.trySolveSymbolInType

class SingletonFunctions {

    static boolean hasSingletonSignature(MethodDeclaration method) {
        return method.static &&
                !method.parameters &&
                returnsSameTypeAsDeclaredType(method)
    }

    static boolean returnsSameTypeAsDeclaredType(MethodDeclaration method) {
        Type returnType = method.type
        method.getAncestorOfType(TypeDeclaration).map { declaredInType ->
            declaredInType.nameAsString == returnType.toString()
        }.orElse(false)
    }

    static Optional<ResolvedValueDeclaration> tryFindUniquelyReturnedFieldValue(MethodDeclaration method, JavaParserFacade solver) {
        List<ReturnStmt> returns = method.findAll(ReturnStmt).findAll { it.expression.present }
        if (returns.size() != 1) {
            return Optional.empty()
        }

        ReturnStmt returnStmt = returns.first()
        returnStmt.expression
            .filter { returnExpression -> returnExpression.nameExpr || returnExpression.fieldAccessExpr }
            .map { returnExpression -> trySolveReturnExpression(returnExpression, solver) }
            .filter { reference -> reference.solved }
            .map { reference -> reference.correspondingDeclaration }
    }

    private static SymbolReference<? extends ResolvedValueDeclaration> trySolveReturnExpression(Expression expression, JavaParserFacade solver) {
        SymbolReference<? extends ResolvedValueDeclaration> reference
        if (expression.nameExpr) {
            reference = trySolve(solver, expression.asNameExpr().name)
        } else {
            Expression scope = expression.asFieldAccessExpr().scope
            if (!scope.nameExpr) {
                reference = SymbolReference.unsolved(ResolvedValueDeclaration)
            } else {
                SymbolReference<ResolvedTypeDeclaration> scopeType =
                        JavaParserFactory.getContext(expression, solver.typeSolver)
                                         .solveType(scope.asNameExpr().nameAsString, solver.typeSolver)
                if (!scopeType.solved) {
                    reference = SymbolReference.unsolved(ResolvedValueDeclaration)
                } else {
                    reference = trySolveSymbolInType(solver.symbolSolver,
                            scopeType.correspondingDeclaration,
                            expression.asFieldAccessExpr().nameAsString)
                }
            }
        }
        
        reference
    }

    static Optional<FieldDeclaration> tryGetAsJPField(ResolvedValueDeclaration value) {
        if (!value.field) {
            return Optional.empty()
        }
        ResolvedFieldDeclaration field = value.asField()
        if (!field.static) {
            return Optional.empty()
        }
        if (!field instanceof JavaParserFieldDeclaration) {
            return Optional.empty()
        }
        Optional.of((field as JavaParserFieldDeclaration).wrappedNode)
    }

    static boolean assignsNewInstance(Node context, String name, Type type) {
        List<AssignExpr> assignExprs = context.findAll(AssignExpr)

        assignExprs.any {
            boolean targetsReturned = it.target.nameExpr && it.target.asNameExpr().nameAsString == name
            boolean createsNewObject = it.value.objectCreationExpr && (it.value.asObjectCreationExpr().type == type)
            targetsReturned && createsNewObject
        }
    }

    static boolean isVariableInitializedByCreatingObjectOfType(VariableDeclarator variable, Type type) {
        variable.initializer.filter { it ->
            it.objectCreationExpr && (it.asObjectCreationExpr().type == type)
        }.present
    }

    static boolean isFieldInitializedByCreatingObjectOfType(FieldDeclaration field, Type type) {
        tryGetFirstVariable(field).filter { variable ->
            isVariableInitializedByCreatingObjectOfType(variable, type)
        }.present
    }

    static boolean isFieldInitializedOnlyInStaticBlock(FieldDeclaration field, Type type) {
        tryGetFirstVariable(field)
                .filter { variable -> !variable.initializer.present }
                .filter { variable -> isFieldInitializedInStaticBlock(field, variable.nameAsString, type) }
                .present
    }

    static boolean isFieldInitializedInStaticBlock(FieldDeclaration field, String name, Type type) {
        field.getAncestorOfType(TypeDeclaration).filter { declaredType ->
            List<InitializerDeclaration> staticInitializers = declaredType.findAll(InitializerDeclaration).findAll { it.static }
            staticInitializers.any { staticInitializer ->
                assignsNewInstance(staticInitializer, name, type)
            }
        }.present
    }

    static boolean isValueInitializedByCreatingObjectOfType(ResolvedValueDeclaration value, Type type) {
        tryGetAsJPField(value).filter { jpField ->
            isFieldInitializedByCreatingObjectOfType(jpField, type)
        }.present
    }

    static boolean isValueInitializedInStaticBlockByCreatingObjectOfType(ResolvedValueDeclaration value, Type type) {
        tryGetAsJPField(value).filter { jpField ->
            isFieldInitializedOnlyInStaticBlock(jpField, type)
        }.present
    }

    static Optional<VariableDeclarator> tryGetFirstVariable(FieldDeclaration field) {
        if (field.variables.size() != 1) {
            return Optional.empty()
        }
        return Optional.of(field.variables.first())
    }
}
