package com.aurea.methobase

import com.aurea.ast.common.UnitHelper
import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import com.github.javaparser.ast.CompilationUnit
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path

import static java.nio.file.Paths.get

class TypeVisitorParentResolutionSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    private File projectRoot
    private File last

    def setup() {
        projectRoot = folder.newFolder("project-root")
    }

    def "parent in the same package"() {
        expect:
        List<ClassMetaInformation> metas = withJavaFile(get("foo", "Base.java"), '''

        package foo;

        class Base {}

        ''').withJavaFile(get("foo", "Foo.java"), '''

        package foo;

        class Foo extends Base {}
        
        ''').runOnLastJavaFile()

        metas[0].extendedType == 'foo.Base'
    }

    def "parent in the other package"() {
        expect:
        List<ClassMetaInformation> metas = withJavaFile(get("bar", "Base.java"), '''
        package bar;

        class Base {}

        ''').withJavaFile(get("foo", "Foo.java"), '''

        package foo;
        
        import bar.Base;

        class Foo extends Base {}
        
        ''').runOnLastJavaFile()

        metas[0].extendedType == 'bar.Base'
    }

    def "parent has full name"() {
        expect:
        List<ClassMetaInformation> metas = withJavaFile(get("bar", "Base.java"), '''
        package bar;

        class Base {}

        ''').withJavaFile(get("foo", "Foo.java"), '''

        package foo;
        
        class Foo extends bar.Base {}
        
        ''').runOnLastJavaFile()

        metas[0].extendedType == 'bar.Base'
    }

    def "parent is imported via wildcard"() {
        expect:
        List<ClassMetaInformation> metas = withJavaFile(get("bar", "Base.java"), '''
        package bar;

        class Base {}

        ''').withJavaFile(get("foo", "Foo.java"), '''

        package foo;

        import bar.*;
        
        class Foo extends Base {}
        
        ''').runOnLastJavaFile()

        metas[0].extendedType == 'bar.Base'
    }

    def "parent is imported via wildcard and there are other wildcards"() {
        expect:
        List<ClassMetaInformation> metas = withJavaFile(get("bar", "Base.java"), '''
        package bar;

        class Base {}

        ''').withJavaFile(get("foo", "Foo.java"), '''

        package foo;

        import xyz.*;
        import bar.*;
        import zxcv.*;
        
        class Foo extends Base {}
        
        ''').runOnLastJavaFile()

        metas[0].extendedType == 'bar.Base'
    }

    TypeVisitorParentResolutionSpec withJavaFile(Path javaPath, String code) {
        File javaFile = projectRoot.toPath().resolve(javaPath).toFile()
        if (!javaFile.parentFile.exists()) {
            javaFile.parentFile.mkdirs()
        }
        javaFile.text = code

        last = javaFile
        this
    }

    List<ClassMetaInformation> runOnLastJavaFile() {
        JavaSourceFinder finder = new JavaSourceFinder()

        //Seems to be a Groovy bug, addressing SourceFilter.empty() gives errors
        SourceFilter noFilter = new SourceFilter() {
            @Override
            boolean test(Path path) {
                return true
            }
        }
        SymbolSolver solver = new SymbolSolver(finder, this.projectRoot.toPath(), noFilter)

        Optional<CompilationUnit> maybeUnit = UnitHelper.getUnitForCode(last)
        CompilationUnit unit = maybeUnit.orElseThrow {
            throw new IllegalArgumentException("Failed to parse $last")
        }
        TypeVisitor visitor = new TypeVisitor(solver)
        unit.accept(visitor, new Unit(cu: unit, modulePath: get("dummy")))
        visitor.metas
    }
}
