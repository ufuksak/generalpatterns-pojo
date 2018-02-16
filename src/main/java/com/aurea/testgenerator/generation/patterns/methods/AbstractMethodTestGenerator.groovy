package com.aurea.testgenerator.generation.patterns.methods

import com.aurea.testgenerator.generation.MethodLevelTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.stereotype.Component

@Component
abstract class AbstractMethodTestGenerator extends MethodLevelTestGenerator<MethodDeclaration> {
    AbstractMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, VisitReporter visitReporter, NomenclatureFactory nomenclatures) {
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
}
