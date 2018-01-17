package com.aurea.testgenerator.pattern.general

import com.aurea.testgenerator.pattern.ASTPatternMatcher
import com.aurea.testgenerator.pattern.UnitMatchVisitor
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import org.springframework.stereotype.Component

@Component
class HierarchyMatcher extends ASTPatternMatcher {
    @Override
    protected UnitMatchVisitor newVisitor(Unit unit) {
        return new UnitMatchVisitor(unit) {
            @Override
            void visit(ClassOrInterfaceDeclaration n, JavaParserFacade solver) {
                n.extendedTypes.each { matches.add(new HierarchyMatch(n.nameAsString, it.nameAsString)) }
                n.implementedTypes.each { matches.add(new HierarchyMatch(n.nameAsString, it.nameAsString)) }
            }
        }
    }
}
