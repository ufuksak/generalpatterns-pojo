package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.extensions.AssignExprExtension
import com.aurea.testgenerator.generation.AbstractMethodTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import groovy.util.logging.Log4j2
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("manual")
@Log4j2
class SpringControllerDelegatingMethodTestGenerator  extends AbstractMethodTestGenerator {

    SpringControllerDelegatingMethodTestGenerator(JavaParserFacade solver, TestGeneratorResultReporter reporter, CoverageReporter visitReporter, NomenclatureFactory nomenclatures) {
        super(solver, reporter, visitReporter, nomenclatures)
    }

    @Override
    protected TestGeneratorResult generate(MethodDeclaration callableDeclaration, Unit unitUnderTest) {
        return null
    }

    @Override
    protected TestType getType() {
        return null
    }

    @Override
    protected boolean shouldBeVisited(Unit unit, MethodDeclaration callableDeclaration) {
        return super.shouldBeVisited(unit, callableDeclaration) && isRestControllerMehod(callableDeclaration) &&
                callDelegateWithParamValuesAndReturnResults( callableDeclaration) &&
                doNotReassignParameters(callableDeclaration)

    }

    boolean doNotReassignParameters(MethodDeclaration methodDeclaration) {
        List<String> paramNames = getParamNames(methodDeclaration)
        if(paramNames.isEmpty()){
            return true
        }
        List<AssignExpr> assignExprs= methodDeclaration.findAll(AssignExpr)
        List<String> assignedNames = assignExprs.collect {it.target.asNameExpr().nameAsString}
        return  assignedNames.disjoint(paramNames)
    }

    private boolean callDelegateWithParamValuesAndReturnResults(MethodDeclaration methodDeclaration) {
        List<ReturnStmt> returnStatements = method.findAll(ReturnStmt).findAll { it.expression.present }
        if(returnStatements.size()>1){
            return false
        }
        def returnStatement = returnStatements.first();
        Expression expression = returnStatement.expression.get()
        if(!expression instanceof  MethodCallExpr){
            return false
        }
        MethodCallExpr methodCallExpr = expression as MethodCallExpr
        if(methodCallExpr.arguments.any{!it.nameExpr || ! it.literalExpr}){
            return false
        }
        List<String> paramNames = getParamNames(methodDeclaration)
        List<String> usedParameters = methodCallExpr.arguments.findAll {it.nameExpr}.collect {it.asNameExpr().nameAsString}
        return paramNames.containsAll(usedParameters)
    }

    private List<String> getParamNames(MethodDeclaration methodDeclaration) {
        List<String> paramNames = methodDeclaration.parameters.collect { it.name }
        paramNames
    }

    private boolean isRestControllerMehod(MethodDeclaration methodDeclaration) {
        !methodDeclaration.static &&
        hasAnnotation(methodDeclaration, "org.springframework.web.bind.annotation.RequestMapping") &&
                isRestController(methodDeclaration.parentNode)

    }

    boolean isRestController(Optional<Node> node) {
        if(node.isPresent()){
            ClassOrInterfaceDeclaration classDeclaration = node.get() as ClassOrInterfaceDeclaration
            hasAnnotation(classDeclaration, "org.springframework.web.bind.annotation.RestController")
        }
        false
    }

    boolean hasAnnotation(BodyDeclaration bodyDeclaration, String annotatioName){
        bodyDeclaration.annotations.any{solver.getType(it).asReferenceType().qualifiedName.equals(annotatioName)}
    }
}
