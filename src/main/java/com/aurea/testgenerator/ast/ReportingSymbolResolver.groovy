package com.aurea.testgenerator.ast

import com.aurea.testgenerator.config.ProjectConfiguration
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.SymbolResolver
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

import static com.aurea.testgenerator.ast.UnsolvedDeclarationEvent.UnsolvedDeclarationType.DECLARATION
import static com.aurea.testgenerator.ast.UnsolvedDeclarationEvent.UnsolvedDeclarationType.TYPE_CALCULATION
import static com.aurea.testgenerator.ast.UnsolvedDeclarationEvent.UnsolvedDeclarationType.TYPE_RESOLUTION

@Component
class ReportingSymbolResolver implements SymbolResolver {

    private final JavaSymbolSolver solver
    private final ApplicationEventPublisher publisher

    ReportingSymbolResolver(ProjectConfiguration cfg, ApplicationEventPublisher publisher) {
        solver = new JavaSymbolSolver(new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(cfg.srcPath.toFile())
        ))
        this.publisher = publisher
    }

    @Override
    <T> T resolveDeclaration(Node node, Class<T> resultClass) {
        try {
            return solver.resolveDeclaration(node, resultClass)
        } catch (UnsolvedSymbolException use) {
            publisher.publishEvent(new UnsolvedDeclarationEvent(this, node, DECLARATION))
        }
        null
    }

    @Override
    <T> T toResolvedType(Type javaparserType, Class<T> resultClass) {
        try {
            return solver.toResolvedType(javaparserType, resultClass)
        } catch (UnsolvedSymbolException use) {
            publisher.publishEvent(new UnsolvedDeclarationEvent(this, javaparserType, TYPE_RESOLUTION))
        }
        null
    }

    @Override
    ResolvedType calculateType(Expression expression) {
        try {
            return solver.calculateType(expression)
        } catch (UnsolvedSymbolException use) {
            publisher.publishEvent(new UnsolvedDeclarationEvent(this, expression, TYPE_CALCULATION))
        }
        null
    }
}
