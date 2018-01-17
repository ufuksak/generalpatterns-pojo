package com.aurea.testgenerator.pattern.easy

import com.aurea.testgenerator.ast.ASTNodeUtils
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import one.util.streamex.StreamEx
import org.apache.commons.lang.StringUtils

enum EasyLine {
    SETTER{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            !n.isStatic() && n.nameAsString.startsWith('set') &&
                    n.isPublic() &&
                    (n.type.toString() == 'void') && (n.parameters.size() == 1) &&
                    isKindOfSmall(n)
        }
    },
    GETTER{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            n.isPublic() && n.nameAsString.startsWith('get') &&
                    (n.type.toString() != 'void') && n.parameters.isEmpty() &&
                    isSmallGetter(n)

        }
    },
    EQUALS{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            return n.nameAsString == 'equals' && n.type.toString() == 'boolean' &&
                    n.parameters.size() == 1 && n.parameters[0].type.toString() == 'Object'
        }
    },
    HASH_CODE{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            return n.nameAsString == 'hashCode' && n.type.toString() == 'int' && n.parameters.isEmpty()
        }
    },
    TO_STRING{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            return n.nameAsString == 'toString' && n.type.toString() == 'String' && n.parameters.isEmpty()
        }
    },
    SINGLETON{
        @Override
        boolean is(MethodDeclaration n, JavaParserFacade facade) {
            if (n.isStatic() && n.nameAsString.toLowerCase().contains('instance') && n.parameters.isEmpty()) {
                ClassOrInterfaceDeclaration coid = ASTNodeUtils.findParentOf(ClassOrInterfaceDeclaration.class, n)
                int distance = StringUtils.getLevenshteinDistance(n.type.toString(), coid.nameAsString)
                return n.type.toString() == coid.nameAsString || distance <= 4
            }
            return false
        }
    }

    abstract boolean is(MethodDeclaration n, JavaParserFacade facade)

    static boolean isSmallGetter(MethodDeclaration n) {
        isKindOfSmall(n) && hasOnlyReturnStatement(n) && hasNoMethodCalls(n)
    }

    static boolean isKindOfSmall(MethodDeclaration n) {
        n.childNodes.size() < 10
    }

    static boolean hasOnlyReturnStatement(MethodDeclaration n) {
        Optional<BlockStmt> blockStmt = ASTNodeUtils.findChildOf(BlockStmt.class, n)
        return blockStmt.map {
            List<Node> filtered = StreamEx.of(it.childNodes).filter { !Comment.class.isAssignableFrom(it.class) }.toList()
            filtered.size() == 1 && (filtered.get(0) instanceof ReturnStmt)
        }.orElse(false)
    }

    static boolean hasNoMethodCalls(MethodDeclaration n) {
        ASTNodeUtils.findChildsOf(MethodCallExpr.class, n).isEmpty()
    }
}

