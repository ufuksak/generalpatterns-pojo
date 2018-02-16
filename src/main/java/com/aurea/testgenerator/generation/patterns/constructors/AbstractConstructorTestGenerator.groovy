package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.generation.MethodLevelTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
abstract class AbstractConstructorTestGenerator extends MethodLevelTestGenerator<ConstructorDeclaration> {

    ValueFactory valueFactory

    AbstractConstructorTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, VisitReporter visitReporter, NomenclatureFactory nomenclatures, ValueFactory valueFactory) {
        super(solver, reporter, visitReporter, nomenclatures)
        this.valueFactory = valueFactory
    }

    protected VoidVisitorAdapter<JavaParserFacade> createVisitor(Unit unit, List<TestGeneratorResult> results) {
        new VoidVisitorAdapter<JavaParserFacade>() {
            @Override
            void visit(ConstructorDeclaration constructorDeclaration, JavaParserFacade arg) {
                visit(constructorDeclaration, unit, results)
            }
        }
    }
}
