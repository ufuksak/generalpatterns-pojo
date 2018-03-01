package com.aurea.testgenerator.generation.assertions

import com.aurea.common.JavaClass
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.ast.TestDependency
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.generation.source.Imports
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SoftAssertions {

    NomenclatureFactory nomenclatureFactory

    @Autowired
    SoftAssertions(NomenclatureFactory nomenclatureFactory) {
        this.nomenclatureFactory = nomenclatureFactory
    }

    Collection<DependableNode<Statement>> softly(JavaClass javaClass, String testName, AssertionProducer... producers) {
        softly(javaClass, testName, Arrays.asList(producers))
    }

    Collection<DependableNode<Statement>> softly(JavaClass javaClass, String testName, AssertionProducer producer) {
        softly(javaClass, testName, Collections.singletonList(producer))
    }

    Collection<DependableNode<Statement>> softly(JavaClass javaClass, String testName, List<AssertionProducer> assertionProducers) {
        List<DependableNode<MethodCallExpr>> assertions = StreamEx.of(assertionProducers)
                                                                  .flatMap { it.assertions.stream() }
                                                                  .toList()
        if (!assertions) {
            return Collections.emptyList()
        }

        asSoftStatements(javaClass, testName, assertions)
    }

    private List<DependableNode<Statement>> asSoftStatements(JavaClass javaClass, String testName, List<DependableNode<MethodCallExpr>> assertions) {
        List<DependableNode<Statement>> softAssertions = new ArrayList<>(assertions.size() + 2)
        String saVariableName = nomenclatureFactory.getVariableNomenclature(javaClass, testName).requestVariableName("SoftAssertions")
        NameExpr sa = new NameExpr(saVariableName)
        softAssertions << DependableNode.from(
                JavaParser.parseStatement("SoftAssertions sa = new SoftAssertions();"),
                new TestDependency(imports: [Imports.SOFT_ASSERTIONS]))

        assertions.each { assertion ->
            MethodCallExpr assertThatMethodCall = assertion.node.scope.get().asMethodCallExpr()
            assertThatMethodCall.setScope(sa)
            softAssertions << DependableNode.from(new ExpressionStmt(assertion.node), assertion.dependency)
        }
        softAssertions << DependableNode.from(JavaParser.parseStatement("sa.assertAll();"))
    }
}
