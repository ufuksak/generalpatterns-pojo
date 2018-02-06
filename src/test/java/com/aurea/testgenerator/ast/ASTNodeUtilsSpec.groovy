package com.aurea.testgenerator.ast

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import spock.lang.Specification

import static com.aurea.util.UnitHelper.getUnitForCode

class ASTNodeUtilsSpec extends Specification {
    private JavaParserFacade facade

    ClassOrInterfaceDeclaration fromCode(String fooClassCode, String parentClassCode) {
        CompilationUnit cu = getUnitForCode(fooClassCode).get().cu

        File tempDir = File.createTempDir("ast-node-utils", "test")
        tempDir.with {
            deleteOnExit()
        }

        new File(tempDir, "Foo.java").with {
            deleteOnExit()
            write(fooClassCode)
        }

        new File(tempDir, "Parent.java").with {
            deleteOnExit()
            write(parentClassCode)
        }

        TypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(tempDir),
        )

        facade = JavaParserFacade.get(solver)
        return cu.getClassByName('Foo').get()
    }

    def "GIVEN a nested method call node WHEN I call the findParents passing this node THEN it returns all parents"() {

        given:
        Optional<Unit> unit = getUnitForCode('''public class NestedMethodCallClass {
                                                                            
                                                public String nestedMethodCall() { 
                                                   return StringUtils.chomp(String.valueOf(100), "0");
                                                }
                                                                        
                                            }''')

        Node nestedMethodCall = unit.get().cu.getNodesByType(MethodCallExpr)[1]

        when:
        List<Node> parents = ASTNodeUtils.parents(nestedMethodCall).toList()

        then:
        parents.size() == 6
        parents[0] == nestedMethodCall.parentNode.get()
        parents[1] == nestedMethodCall.parentNode.get().parentNode.get()
        parents[2] == nestedMethodCall.parentNode.get().parentNode.get().parentNode.get()
        parents[3] == nestedMethodCall.parentNode.get().parentNode.get().parentNode.get().parentNode.get()
        parents[4] == nestedMethodCall.parentNode.get().parentNode.get().parentNode.get().parentNode.get().parentNode.get()

        parents[5] == unit.get().cu &&
                parents[5] == nestedMethodCall.parentNode.get().parentNode.get().parentNode.get().parentNode.get().parentNode.get().parentNode.get()

    }

    def "GIVEN a nested method call node and filter (name == chomp) WHEN I call the findParents passing this node and the filter THEN it return the chomp method call"() {

        given:
        Optional<Unit> unit = getUnitForCode('''public class NestedMethodCallClass {
                                                                            
                                                public String nestedMethodCall() { 
                                                   return StringUtils.chomp(String.valueOf(100), "0");
                                                }
                                                                        
                                            }''')

        Node nestedMethodCall = unit.get().cu.getNodesByType(MethodCallExpr)[1]

        when:
        List<MethodCallExpr> parents = ASTNodeUtils.parents(nestedMethodCall, MethodCallExpr, { it.nameAsString == "chomp" }).toList()

        then:
        parents.size() == 1
        parents[0] == nestedMethodCall.parentNode.get()
        parents[0].nameAsString == "chomp"
        ((NameExpr) parents[0].scope.get()).nameAsString == "StringUtils"

    }
}
