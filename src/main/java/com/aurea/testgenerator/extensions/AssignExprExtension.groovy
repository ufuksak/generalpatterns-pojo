package com.aurea.testgenerator.extensions

import com.github.javaparser.ast.expr.AssignExpr
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Component
@Log4j2
class AssignExprExtension implements ASTExtension {
    AssignExprExtension() {
        log.debug "Adding AssignExpr::targetsThis"
        AssignExpr.metaClass.targetsThis() {
            AssignExpr n = delegate as AssignExpr
            n.target.fieldAccessExpr && n.target.asFieldAccessExpr().scope.thisExpr
        }
    }
}
