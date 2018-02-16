package com.aurea.testgenerator.generation.patterns.constructors

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.Callability
import com.aurea.testgenerator.ast.InvocationBuilder
import com.aurea.testgenerator.generation.DependableNode
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestGeneratorResultReporter
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.VisitReporter
import com.aurea.testgenerator.generation.merge.TestNodeMerger
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.names.TestMethodNomenclature
import com.aurea.testgenerator.generation.source.Imports
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.value.ValueFactory
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.transform.ToString
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ToString
@Log4j2
class IsInstantiableConstructorGenerator extends AbstractConstructorTestGenerator {

    @Autowired
    IsInstantiableConstructorGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, VisitReporter visitReporter, NomenclatureFactory nomenclatures, ValueFactory valueFactory) {
        super(solver, reporter, visitReporter, nomenclatures, valueFactory)
    }

    @Override
    TestGeneratorResult generate(ConstructorDeclaration constructorDeclaration, Unit unitUnderTest) {
        Optional<DependableNode<ObjectCreationExpr>> constructorCall = new InvocationBuilder(valueFactory).build(constructorDeclaration)
        TestGeneratorResult result = new TestGeneratorResult()
        constructorCall.ifPresent { constructCallExpr ->
            TestMethodNomenclature testMethodNomenclature = nomenclatures.getTestMethodNomenclature(unitUnderTest.javaClass)
            DependableNode<MethodDeclaration> isInstantiable = new DependableNode<>()
            TestNodeMerger.appendDependencies(isInstantiable, constructCallExpr)
            String testText = ''
            String testName = testMethodNomenclature.requestTestMethodName(getType(), constructorDeclaration)
            try {
                testText = """
                @Test
                public void ${testName}() throws Exception {
                    ${constructCallExpr.node};
                }                              
                """
                isInstantiable.node = JavaParser.parseBodyDeclaration(testText)
                                                .asMethodDeclaration()
                isInstantiable.dependency.imports << Imports.JUNIT_TEST

                result.tests = [isInstantiable]
            } catch (ParseProblemException ppe) {
                log.error "Failed to parse $testText", ppe
                result.errors << new TestGeneratorError(cause: "Failed to parse $testText")
            }
        }
        result
    }

    @Override
    TestType getType() {
        ConstructorTypes.EMPTY_CONSTRUCTOR
    }

    @Override
    boolean shouldBeVisited(Unit unit, ConstructorDeclaration callableDeclaration) {
        Callability.isCallableFromTests(callableDeclaration) && ASTNodeUtils.parents(callableDeclaration, TypeDeclaration).noneMatch { it.enumDeclaration }
    }
}
