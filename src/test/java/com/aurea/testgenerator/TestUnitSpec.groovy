package com.aurea.testgenerator

import com.aurea.common.JavaClass
import com.aurea.testgenerator.extensions.Extensions
import com.aurea.testgenerator.generation.TestUnit
import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Paths


class TestUnitSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    static TestUnit newTestUnit() {
        new TestUnit(new Unit(
                new CompilationUnit("sample"),
                new JavaClass("sample.Foo"),
                Paths.get("")
        ))
    }

    def setupSpec() {
        Extensions.enable()
    }

    Type wrapWithCompilationUnit(Type type) {
        CompilationUnit cu = new CompilationUnit("sample")
        injectSolver(cu)

        ClassOrInterfaceDeclaration coid = cu.addClass("Foo")
        coid.addField(type, "anyfield")
        type
    }

    JavaSymbolSolver getSymbolSolver() {
        new JavaSymbolSolver(new CombinedTypeSolver(
                new JavaParserTypeSolver(folder.root),
                new ReflectionTypeSolver()
        ))
    }

    void injectSolver(CompilationUnit cu) {
        getSymbolSolver().inject(cu)
    }

    JavaParserFacade getJavaParserFacade() {
        JavaParserFacade.get(new CombinedTypeSolver(
                new JavaParserTypeSolver(folder.root),
                new ReflectionTypeSolver()
        ))
    }
}
