package com.aurea.testgenerator.reporting

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.aurea.testgenerator.ast.UnsolvedDeclarationEvent
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt
import groovy.util.logging.Log4j2
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentLinkedQueue

@Component
@Log4j2
@Profile("statistics")
class UnsolvedSymbolLog implements ApplicationListener<UnsolvedDeclarationEvent> {

    private final ConcurrentLinkedQueue descriptions = new ConcurrentLinkedQueue()

    @Override
    void onApplicationEvent(UnsolvedDeclarationEvent event) {
        Node node = event.node
        String description = ASTNodeUtils.getNameOfCompilationUnit(node) + "::"

        if (node instanceof NodeWithSimpleName) {
            description += (node as NodeWithSimpleName).nameAsString
        } else if (node instanceof FieldDeclaration) {
            FieldDeclaration field = node as FieldDeclaration
            if (field.variables) {
                description += field.variables.first().nameAsString
            }
        } else if (node instanceof ThisExpr) {
            description += (node as ThisExpr).getAncestorOfType(TypeDeclaration).map {
                "this in " + it.nameAsString
            }.orElse("")
        } else if (node instanceof ExplicitConstructorInvocationStmt) {
            description += node.toString()
        }
        descriptions.add(description)
    }

    @PreDestroy
    void log() {
        log.debug """
Unsolved nodes:
\t${descriptions.toSet().sort().join(System.lineSeparator() + "\t")}
"""
    }
}
