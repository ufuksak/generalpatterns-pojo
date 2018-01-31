package com.aurea.testgenerator.pattern.easy

import com.aurea.coverage.unit.MethodCoverage
import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.ast.FieldResolver
import com.aurea.testgenerator.coverage.CoverageService
import com.aurea.testgenerator.coverage.MethodCoverageQuery
import com.aurea.testgenerator.pattern.AbstractSubjectMethodMatcher
import com.aurea.testgenerator.pattern.PatternMatch
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.aurea.testgenerator.ast.ASTNodeUtils.*

@Component
class AccessorMatcher extends AbstractSubjectMethodMatcher {

    @Autowired
    AccessorMatcher(CoverageService coverageService, JavaParserFacade javaParserFacade) {
        this.coverageService = coverageService
        this.solver = javaParserFacade
    }

    @Override
    Optional<? extends PatternMatch> matchMethod(Unit unit, MethodDeclaration n) {
        if (isUncoveredGetter(unit, n, solver)) {
            Optional<? extends AccessorMatch> maybeMatch = createMatch(unit, n, solver)
            return maybeMatch.isPresent() ? addCoreInfo(maybeMatch.get(), unit, n, solver) : Optional.empty()
        }
        Optional.empty()
    }

    private Optional<AccessorMatch> addCoreInfo(AccessorMatch match, Unit unit, MethodDeclaration n, JavaParserFacade facade) {
        MethodCoverageQuery query = MethodCoverageQuery.of(unit, n)
        MethodCoverage coverage = coverageService.getMethodCoverage(query)
        match.lines = coverage.uncovered
        return Optional.of(match)
//        if (!match.constantReturnValue.isPresent()) {
//            boolean hasANotNameExpr = StreamEx.of(returnStmt.childNodes).anyMatch { it.class != NameExpr.class }
//            if (hasANotNameExpr) {
//                return Optional.empty()
//            }
//            Optional<NameExpr> fieldReferenceName = findChildOf(NameExpr.class, returnStmt)
//            if (fieldReferenceName.isPresent()) {
//                return findMatch(match, n, fieldReferenceName.get(), facade)
//            }
//        } else {
//            match.enumDeclaration = findDefaultEnum(n)
//        }
    }

    Optional<? extends AccessorMatch> createMatch(Unit unit, MethodDeclaration n, JavaParserFacade facade) {}


    Optional<AccessorMatch> findMatch(AccessorMatch match, MethodDeclaration n, NameExpr fieldReference, JavaParserFacade facade) {
        SymbolReference<? extends ResolvedValueDeclaration> symbolReference = facade.solve(fieldReference)
        TypeDeclaration classOrEnum = findParentSubTypeOf(TypeDeclaration.class, n)
        ResolvedValueDeclaration declaration
        if (symbolReference.isSolved()) {
            declaration = symbolReference.correspondingDeclaration
        } else {
            EnumConstantDeclaration inEnumConstant = findParentSubTypeOf(EnumConstantDeclaration.class, fieldReference)
            if (inEnumConstant != null) {
                match.enumDeclaration = Optional.of(inEnumConstant)
                List<FieldDeclaration> fieldDeclarations = findChildsSubTypesOf(FieldDeclaration.class, inEnumConstant)
                Optional<VariableDeclarator> declarator = StreamEx.of(fieldDeclarations).flatMap { it -> it.getVariables().stream() }
                        .findFirst { it.nameAsString == fieldReference.nameAsString }
                if (declarator.isPresent()) {
                    VariableDeclarator variableDeclarator = declarator.get()
                    FieldDeclaration fieldDeclaration = variableDeclarator.getAncestorOfType(FieldDeclaration.class).get()
                    return processFieldDeclaration(fieldDeclaration, variableDeclarator, classOrEnum, match)
                } else {
                    return Optional.empty()
                }
            } else {
                return Optional.empty()
            }
        }
        if (declaration.isField()) {
            VariableDeclarator variableDeclarator = getVariableDeclarator(declaration, classOrEnum, javaParserFacade)
            FieldDeclaration fieldDeclaration = variableDeclarator.getAncestorOfType(FieldDeclaration.class).get()
            return processFieldDeclaration(fieldDeclaration, variableDeclarator, classOrEnum, match)
        }
        return Optional.of(match)
    }

    private Optional<AccessorMatch> processFieldDeclaration(FieldDeclaration fieldDeclaration, VariableDeclarator variableDeclarator, TypeDeclaration classOrEnum, AccessorMatch match) {
        match.fieldName = Optional.of(variableDeclarator)
        if (fieldDeclaration.isFinal() && fieldDeclaration.isStatic()) {
            Optional<Expression> initializingExpression = variableDeclarator.getInitializer()
            match.constantReturnValue = initializingExpression.map {
                Optional.of(
                        ConstantReturnValue.literal(initializingExpression.get().toString()))
            }.orElseGet {
                StreamEx.of(classOrEnum.getNodesByType(InitializerDeclaration.class)).map {
                    it.getNodesByType(AssignExpr.class)
                }.flatMap { assignExprs -> assignExprs.stream() }.findFirst { assignExpr ->
                    assignExpr.target instanceof NameExpr && (assignExpr.target as NameExpr).name == variableDeclarator.name &&
                            LiteralStringValueExpr.class.isAssignableFrom(assignExpr.value.class)
                }.map { assignExpr ->
                    Optional.of(
                            ConstantReturnValue.literal((assignExpr.value as LiteralStringValueExpr).value))
                }.orElse(Optional.empty())
            }
        } else if (fieldDeclaration.isFinal()) {
            Optional<Expression> initializingExpression = variableDeclarator.getInitializer()
            if (initializingExpression.isPresent()) {
                match.constantReturnValue = initializingExpression.map { it ->
                    Optional.of(
                            ConstantReturnValue.objectConstant(it.toString()))
                }.orElse(Optional.empty())
            } else {
                Optional<Optional<ConstructorNewInstanceExpression>> maybeViaConstructor = findConstructorInitialization(variableDeclarator, classOrEnum)
                if (maybeViaConstructor.isPresent()) {
                    match.constructorInitialization = maybeViaConstructor.get()
                } else {
                    return Optional.empty()
                }
            }
        } else if (!fieldDeclaration.isPrivate() && !fieldDeclaration.isProtected()) {
            match.canBeAccessedDirectly = true
        } else {
            match.modifier = findSetter(fieldDeclaration, classOrEnum)
        }
        return Optional.of(match)
    }

    Optional<MethodDeclaration> findSetter(FieldDeclaration fieldDeclaration, TypeDeclaration classOrEnum) {
        VariableDeclarator variable = fieldDeclaration.getVariable(0)
        String fieldName = variable.getNameAsString()
        String setterName = 'set' + StringUtils.capitalize(fieldName)
        List<MethodDeclaration> setters = classOrEnum.getMethodsByName(setterName)
        List<MethodDeclaration> simpleSetters = StreamEx.of(setters).filter {
            EasyLine.SETTER.is(it, javaParserFacade)
        }.toList()
        return simpleSetters.size() == 1 ? Optional.of(simpleSetters.get(0)) : Optional.empty()
    }

    static VariableDeclarator getVariableDeclarator(ResolvedValueDeclaration declaration,
                                                    TypeDeclaration classOrEnum,
                                                    JavaParserFacade javaParserFacade) {
        String fieldDeclarationName = declaration.asField().name
        List<FieldDeclaration> fields
        if (classOrEnum instanceof ClassOrInterfaceDeclaration && !classOrEnum.extendedTypes.isEmpty()) {
            FieldResolver resolver = new FieldResolver(javaParserFacade)
            fields = resolver.getAllAccessibleFields(classOrEnum as ClassOrInterfaceDeclaration)
        } else {
            fields = classOrEnum.fields
        }
        StreamEx.of(fields).map { it.getVariables() }.flatMap { vd -> vd.stream() }.findFirst { vd ->
            vd.nameAsString == fieldDeclarationName
        }.get()
    }

    Optional<Optional<ConstructorNewInstanceExpression>> findConstructorInitialization(VariableDeclarator vd, TypeDeclaration td) {
        List<ConstructorDeclaration> constructors = findChildsOf(ConstructorDeclaration.class, td)
        return StreamEx.of(constructors).filter { it -> !it.isPrivate() && !it.isProtected() }.map { constructor ->
            List<AssignExpr> assignExprs = constructor.getNodesByType(AssignExpr.class)
            Optional<ConstructorNewInstanceExpression> constructorInitialization = StreamEx.of(assignExprs).filter {
                it.target instanceof FieldAccessExpr
            }.map { assignExpr ->
                ConstructorNewInstanceExpression constructorInitialization = null
                FieldAccessExpr fieldAccessExpr = assignExpr.target as FieldAccessExpr
                boolean isAccessingThis = fieldAccessExpr.scope.map { it -> it instanceof ThisExpr }.orElse(false)
                if (isAccessingThis) {
                    if (fieldAccessExpr.name == vd.name) {
                        if (assignExpr.value instanceof NameExpr) {
                            constructorInitialization = findAccordingConstructorParameter(assignExpr, constructor, javaParserFacade)
                        } else if (LiteralStringValueExpr.class.isAssignableFrom(assignExpr.value.class)) {
                            LiteralStringValueExpr literal = assignExpr.value as LiteralStringValueExpr
                            constructorInitialization = new ConstructorNewInstanceExpression(constructorDeclaration: constructor, constantInitialValue: literal.value)
                        }
                    }
                }
                return constructorInitialization
            }.findFirst { it -> it != null }

            if (!constructorInitialization.isPresent()) {
                constructorInitialization = StreamEx.of(assignExprs).filter { assignExpr ->
                    assignExpr.target instanceof NameExpr && (assignExpr.target as NameExpr).name == vd.name
                }.map { assignExpr ->
                    if (assignExpr.value instanceof NameExpr) {
                        return findAccordingConstructorParameter(assignExpr, constructor, javaParserFacade)
                    }
                    return null
                }.findFirst { it -> it != null }
            }
            return constructorInitialization
        }.findFirst { it -> it.isPresent() }
    }

    static ConstructorNewInstanceExpression findAccordingConstructorParameter(AssignExpr assignExpr, ConstructorDeclaration constructor, JavaParserFacade javaParserFacade) {
        NameExpr name = assignExpr.value as NameExpr
        SymbolReference reference = javaParserFacade.solve(name)
        if (reference.correspondingDeclaration.isParameter()) {
            String parameterName
            if (reference.correspondingDeclaration instanceof JavaParserParameterDeclaration) {
                parameterName = (reference.correspondingDeclaration as JavaParserParameterDeclaration).name
            } else {
                ResolvedParameterDeclaration parameterDeclaration = reference.correspondingDeclaration.asParameter()
                parameterName = parameterDeclaration.name
            }

            Optional<Parameter> parameter = StreamEx.of(constructor.parameters).findFirst {
                it.nameAsString == parameterName
            }
            if (parameter.isPresent()) {
                return new ConstructorNewInstanceExpression(constructorDeclaration: constructor, constructorParameterInitializer: parameter.get())
            }
        }
        return null
    }

    @Override
    boolean shouldBeVisited(Unit unit) {
        List<ClassOrInterfaceDeclaration> types = findChildsSubTypesOf(ClassOrInterfaceDeclaration, unit.cu)
        return super.shouldBeVisited(unit) && !types.isEmpty() && !types.first().isAbstract()
    }

    private boolean isUncoveredGetter(Unit unit, MethodDeclaration n, JavaParserFacade facade) {
        EasyLine.GETTER.is(n, facade) && !EasyLine.SINGLETON.is(n, facade) && isNotCovered(unit, n)
    }
}
