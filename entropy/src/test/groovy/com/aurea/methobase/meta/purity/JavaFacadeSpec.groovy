package com.aurea.methobase.meta.purity

import com.aurea.ast.common.UnitHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.MethodUsage
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
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

        fileWithCode '''
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


    def "java facade knows about java.util classes and can figure out method calls to it"() {
        given:
        File classFile = fileWithCode '''
        import java.util.List;
        import java.util.ArrayList;
        
        class Foo {
            int foo() {
                List<Integer> ints = new ArrayList<>();
                return ints.size();
                
            } 
        }
        ''', 'Foo'
        MethodDeclaration md = getFirstMethodDeclaration(classFile)
        MethodCallExpr sizeMethodCallExpr = md.findAll(MethodCallExpr).first()
        JavaParserFacade facade = getFacade()

        when:
        MethodUsage usage = facade.solveMethodAsUsage(sizeMethodCallExpr)

        then:
        usage.returnType().isPrimitive()
        usage.returnType().asPrimitive() == ResolvedPrimitiveType.INT
    }

    def "java facade can resolve static method calls from another class in the same directory"() {
        given:
        fileWithCode '''
            package foo;

            import foo.Bar;
            
            class Foo {
                int foo() {
                    return Bar.bar();
                } 
            }
        ''', 'Foo'

        fileWithCode '''
            package foo;

            class Bar {
                public static int bar() {
                    return 4;
                }
            }
        ''', 'Bar'

        JavaParserFacade facade = getFacade()

        when:
        ResolvedReferenceTypeDeclaration typeDecl = facade.typeSolver.solveType('Bar')

        then:
        println typeDecl
    }

    def "java facade can resolve static method calls from another class from different package"() {
        given:
        fileWithCode '''
            package foo;

            import foo.bar.Bar;
            
            class Foo {
                int foo() {
                    return Bar.bar();
                } 
            }
        ''', 'Foo'

        folder.newFolder("bar")
        File barJava = folder.newFile("bar/Bar.java")
        barJava.text = '''
            package foo.bar;

            class Bar {
                public static int bar() {
                    return 4;
                }
            }
        '''

        JavaParserFacade facade = getFacade()

        when:
        ResolvedReferenceTypeDeclaration typeDecl = facade.typeSolver.solveType('bar.Bar')

        then:
        println typeDecl
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
        CompilationUnit cu = UnitHelper.getUnitForCode(classFile)
        MethodDeclaration md = cu.findAll(MethodDeclaration).first()
        md
    }
}
