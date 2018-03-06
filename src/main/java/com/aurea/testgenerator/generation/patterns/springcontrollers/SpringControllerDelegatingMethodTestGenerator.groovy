package com.aurea.testgenerator.generation.patterns.springcontrollers

import com.aurea.testgenerator.extensions.AssignExprExtension
import com.aurea.testgenerator.generation.AbstractMethodTestGenerator
import com.aurea.testgenerator.generation.TestGeneratorError
import com.aurea.testgenerator.generation.TestGeneratorResult
import com.aurea.testgenerator.generation.TestType
import com.aurea.testgenerator.generation.ast.DependableNode
import com.aurea.testgenerator.generation.names.NomenclatureFactory
import com.aurea.testgenerator.reporting.CoverageReporter
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.type.ClassOrInterfaceType
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
        TestGeneratorResult result = new TestGeneratorResult()
        try{
            List<FieldDeclaration> targetFields = callableDeclaration.parentNode.get().findAll(FieldDeclaration)
            if(targetFields.isEmpty()){
                return result
            }
            List<FieldDeclaration> testFields = targetFields.collect {
                VariableDeclarator variable = it.variables.first()
                new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                        VariableDeclarator(variable.type, variable.name)).addAnnotation("Mock") //TODO add support
                // for primitive types
            }
            ClassOrInterfaceDeclaration classDeclaration = node.get() as ClassOrInterfaceDeclaration
            FieldDeclaration instanceField = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), new
                    VariableDeclarator(new ClassOrInterfaceType(classDeclaration.name), "controllerInstance"))
                    .addAnnotation("@InjectMocks")

            List<DependableNode<VariableDeclarationExpr>> variables = getVariableDeclarations(method)
            List<Statement> variableStatements = variables.collect { new ExpressionStmt(it.node) }

            resolve the delegate method and create a Method call statement

            map the method under test parameters to query parameters

            resolve the request path and method and build the url

            build and return the test method

        }catch (TestGeneratorError tge) {
            result.errors << tge
        }
        return result
    }

    @Override
    protected TestType getType() {
        return SpringControllersTestTypes.DELEGATING
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
        if(!methodCallExpr.scope.isPresent() || !methodCallExpr.scope.get().asNameExpr().resolve().isField()){
            return false
        }
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
            return hasAnnotation(classDeclaration, "org.springframework.web.bind.annotation.RestController")
        }
        false
    }

    boolean hasAnnotation(BodyDeclaration bodyDeclaration, String annotatioName){
        bodyDeclaration.annotations.any{solver.getType(it).asReferenceType().qualifiedName.equals(annotatioName)}
    }
}
