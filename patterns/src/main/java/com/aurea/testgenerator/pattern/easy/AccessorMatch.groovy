package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.source.Unit
import com.aurea.testgenerator.pattern.PatternMatchImpl
import com.aurea.testgenerator.pattern.PatternType
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator

class AccessorMatch extends PatternMatchImpl implements LineCounter {

    public static final ConstructorDeclaration DEFAULT_CONSTRUCTOR = new ConstructorDeclaration()

    MethodDeclaration accessor
    Optional<MethodDeclaration> modifier = Optional.empty()
    Optional<ConstructorDeclaration> constructor = Optional.empty()
    Optional<ConstructorNewInstanceExpression> constructorInitialization = Optional.empty()
    Optional<VariableDeclarator> fieldName = Optional.empty()
    Optional<EnumConstantDeclaration> enumDeclaration = Optional.empty()

    boolean canBeAccessedDirectly
    int lines

    AccessorMatch(Unit unit, MethodDeclaration n) {
        super(unit, n.nameAsString)
        this.accessor = n
    }

    @Override
    PatternType type() {
        return null
    }

    boolean isConstructor() {
        constructor.present && (constructor.get() == DEFAULT_CONSTRUCTOR)
    }

    boolean hasModifier() {
        modifier.present
    }

    boolean isInEnum() {
        enumDeclaration.present
    }
}
