package com.aurea.methobase.meta.purity

import com.aurea.ast.common.UnitHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class JavaFacadeSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    def "with java parser facade it is possible to say whether field is a constant"() {
        given:
        File classFile = fileWithCode '''
        class Foo {
            static final int i = 1;
            int foo() {
                return i;
            } 
        }
        ''', 'Foo'
        MethodDeclaration md = getFirstMethodDeclaration(classFile)
        JavaParserFacade facade = getFacade()

        when:
        SymbolReference<? extends ResolvedValueDeclaration> reference = facade.symbolSolver.solveSymbol("i", md)

        then:
        reference.isSolved()
        reference.getCorrespondingDeclaration().isField()
        ResolvedFieldDeclaration rfd = reference.getCorrespondingDeclaration().asField()
        rfd.isStatic()
        rfd.isFinal()
    }

    def "with java paraser facade it is possible to say whether field declared in parent class is being referenced"() {
        given:
        File fooFile = fileWithCode '''
            class Foo extends Base {
                int foo() {
                    return i;
                } 
            }
        ''', 'Foo'

        File baseFile = fileWithCode '''
            class Base {
                public static final int i = 123;
            }
        ''', 'Base'

        MethodDeclaration md = getFirstMethodDeclaration(fooFile)
        JavaParserFacade facade = getFacade()

        when:
        SymbolReference<? extends ResolvedValueDeclaration> reference = facade.symbolSolver.solveSymbol("i", md)

        then:
        reference.isSolved()
        reference.getCorrespondingDeclaration().isField()
        ResolvedFieldDeclaration rfd = reference.getCorrespondingDeclaration().asField()
        rfd.isStatic()
        rfd.isFinal()
    }

    File fileWithCode(String code, String className) {
        File file = folder.newFile("${className}.java")
        file.text = code
        file
    }

    JavaParserFacade getFacade() {
        JavaParserFacade.get(new CombinedTypeSolver(
                new JavaParserTypeSolver(folder.root),
                new ReflectionTypeSolver()
        ))
    }

    private static MethodDeclaration getFirstMethodDeclaration(File classFile) {
        CompilationUnit cu = UnitHelper.getUnitForCode(classFile).get()
        MethodDeclaration md = cu.findAll(MethodDeclaration).first()
        md
    }
}
