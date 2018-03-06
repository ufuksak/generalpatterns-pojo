package com.aurea.testgenerator.generation

import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

@Component
abstract class AbstractMethodTestGenerator extends MethodLevelTestGenerator<MethodDeclaration> {
    protected ValueFactory valueFactory

    AbstractMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, CoverageReporter visitReporter, NomenclatureFactory nomenclatures) {
        super(solver, reporter, visitReporter, nomenclatures)
    }

    protected VoidVisitorAdapter<JavaParserFacade> createVisitor(Unit unit, List<TestGeneratorResult> results) {
        new VoidVisitorAdapter<JavaParserFacade>() {
            @Override
            void visit(MethodDeclaration methodDeclaration, JavaParserFacade javaParserFacade) {
                visit(methodDeclaration, unit, results)
            }
        }
    }

    protected List<DependableNode<VariableDeclarationExpr>> getVariableDeclarations(MethodDeclaration method) {
        List<DependableNode<VariableDeclarationExpr>> variables = StreamEx.of(method.parameters).map { p ->
            valueFactory.getVariable(p.nameAsString, p.type).orElseThrow {
                new TestGeneratorError("Failed to build variable for parameter $p of $method")
            }
        }.toList()
        variables
    }
}
