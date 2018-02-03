package com.aurea.testgenerator.generation

import com.aurea.testgenerator.source.Unit
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

@Component
@Log4j2
class UnitTestMergeEngine {

    UnitTestMergeResult merge(Unit unitUnderTest, List<TestNodeMethod> uts) {
        new UnitTestMergeEngineProcess(unit: unitUnderTest, uts: uts)
                .merge()
    }

    private static class UnitTestMergeEngineProcess {
        Unit unit
        List<TestNodeMethod> uts
        CompilationUnit cu = new CompilationUnit()
        List<UnitTestMergeConflict> conflicts = []
        ClassOrInterfaceDeclaration coid

        UnitTestMergeResult merge() {
            addPackage()
            addImports()
            addTestClassDeclaration()
            addFields()
            addClassSetups()
            addMethodSetups()
            addTests()

            new UnitTestMergeResult(cu, conflicts)
        }

        private void addPackage() {
            cu.setPackageDeclaration(unit.packageName)
        }

        private addImports() {
            StreamEx.of(uts).flatMap { StreamEx.of(it.dependency.imports) }.toSet().forEach {
                cu.addImport(it)
            }
        }

        private void addTestClassDeclaration() {
            String unitTestName = "${unit.className}Test"
            coid = cu.addClass(unitTestName)
            coid.addModifier(Modifier.PUBLIC)
            appendClassAnnotations()
        }

        private void addFields() {
            StreamEx.of(uts).flatMap { it.dependency.fields.stream() }.toSet().forEach {
                coid.addMember(it)
            }
        }

        private void addClassSetups() {
            StreamEx.of(uts)
                    .flatMap { it.dependency.classSetups.stream() }
                    .forEach {
                coid.addMember(it)
            }
        }

        private void addMethodSetups() {
            StreamEx.of(uts)
                    .flatMap() { it.dependency.methodSetups.stream() }
                    .forEach {
                coid.addMember(it)
            }
        }

        private void addTests() {
            StreamEx.of(uts)
                    .map { it.node }
                    .filter { it.present }
                    .map { it.get() }
                    .forEach {
                coid.addMember(it)
            }
        }

        private void appendClassAnnotations() {
            StreamEx.of(uts).flatMap { it.dependency.classAnnotations.stream() }.toSet().forEach {
                coid.addAnnotation(it)
            }
        }
    }
}
