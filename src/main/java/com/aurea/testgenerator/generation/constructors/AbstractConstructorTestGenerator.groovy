package com.aurea.testgenerator.generation.constructors

import com.aurea.testgenerator.generation.ReportingTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorVisitor
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class AbstractConstructorTestGenerator extends ReportingTestGenerator {

    @Autowired
    JavaParserFacade solver

    @Override
    Collection<TestGeneratorResult> generate(Unit unit) {
        TestGeneratorVisitor visitor = new ConstructorVisitor(unit, solver)
        visitor.visit()
    }

    protected abstract TestGeneratorResult generate(ConstructorDeclaration cd)

    protected abstract TestType getType()

    protected boolean shouldBeVisited(Unit unit, ConstructorDeclaration cd) {
        true
    }

    private class ConstructorVisitor extends TestGeneratorVisitor {
        ConstructorVisitor(Unit unit, JavaParserFacade solver) {
            super(unit, solver)
        }

        @Override
        void visit(ConstructorDeclaration n, JavaParserFacade arg) {
            if (shouldBeVisited(unit, n)) {
                TestGeneratorResult result = generate(n)
                result.type = getType()
                publish(result, unit, n)
                results << result
            }
        }
    }
}
