package com.aurea.testgenerator.pattern

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import org.springframework.stereotype.Component


@Component
class SandboxMatcher extends AbstractSubjectMethodMatcher {
    @Override
    Optional<? extends PatternMatch> matchMethod(Unit unit, MethodDeclaration n) {
        logger.info "Name $n.nameAsString"
        Optional.empty()
    }
}
