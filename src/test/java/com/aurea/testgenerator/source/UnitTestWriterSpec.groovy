package com.aurea.testgenerator.source

import com.aurea.common.JavaClass
import com.aurea.testgenerator.config.ProjectConfiguration
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import static org.assertj.core.api.Assertions.*


class UnitTestWriterSpec extends Specification {

    private static final String FOO_TEST_TEXT = '''
        package sample;
        
        class FooTest {
        }
    '''

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    UnitTestWriter writer
    ProjectConfiguration projectCfg
    Path testFolder
    Path srcFolder
    FileTreeBuilder testBuilder

    def setup() {
        testFolder = folder.newFolder("test").toPath()
        srcFolder = folder.newFolder("src").toPath()
        projectCfg = new ProjectConfiguration()
        projectCfg.src = srcFolder
        projectCfg.out = testFolder
        projectCfg.fileNameResolution = FileNameConflictResolutionStrategyType.OVERRIDE
        writer = newWriter()
        testBuilder = new FileTreeBuilder(testFolder.toFile())
    }

    def "when generation disabled - do not generate any test"() {
        setup:
        writer = new UnitTestWriter(new ProjectConfiguration(
                src: srcFolder,
                out: testFolder,
                blank: true), [])
        Unit unit = fooTestUnit()

        when:
        writer.write(unit)

        then:
        Files.list(testFolder).count() == 0
    }

    def "when test file doesn't exist - simply write it"() {
        setup:
        Unit unit = fooTestUnit()

        when:
        writer.write(unit)

        then:
        testFolder.resolve('sample/FooTest.java').toFile().exists()
    }

    def "overriding existing files when strategy is override"() {
        setup:
        Unit unit = fooTestUnit()

        testBuilder.dir('sample') {
            file('FooTest.java') {
                text = ''' can be anything, doesn't matter, will be overridden '''
            }
        }

        when:
        writer.write(unit)

        then:
        String content = testFolder.resolve('sample/FooTest.java').toFile().text
        assertThat(content).isEqualToIgnoringWhitespace(FOO_TEST_TEXT)
    }

    def "skipping existing files when strategy is skip"() {
        setup:
        projectCfg.fileNameResolution = FileNameConflictResolutionStrategyType.SKIP
        writer = newWriter()

        Unit unit = fooTestUnit()
        String existingContent = '123'

        testBuilder.dir('sample') {
            file('FooTest.java') {
                text = existingContent
            }
        }

        when:
        writer.write(unit)

        then:
        String content = testFolder.resolve('sample/FooTest.java').toFile().text
        content == existingContent
    }

    def "creates a new file when already existing and strategy is rename"() {
        setup:
        projectCfg.fileNameResolution = FileNameConflictResolutionStrategyType.RENAME
        writer = newWriter()
        Unit unit = fooTestUnit()
        String existingContent = '123'

        testBuilder.dir('sample') {
            file('FooTest.java') {
                text = existingContent
            }
            file('FooTest2.java') {
                text = existingContent
            }
        }

        when:
        writer.write(unit)

        then:
        String content = testFolder.resolve('sample/FooTest3.java').toFile().text
        assertThat(content).isEqualToIgnoringWhitespace(FOO_TEST_TEXT)
        testFolder.resolve('sample/FooTest.java').toFile().exists()
        testFolder.resolve('sample/FooTest2.java').toFile().exists()
    }

    private UnitTestWriter newWriter() {
        new UnitTestWriter(projectCfg, [new OverrideClassWriteStrategy(),
                                        new RenameClassWriteStrategy(),
                                        new SkipClassWriteStrategy()])
    }

    Unit fooTestUnit() {
        testUnit JavaParser.parse(FOO_TEST_TEXT)
    }

    Unit testUnit(CompilationUnit cu) {
        JavaClass javaClass = new JavaClass(
                cu.packageDeclaration.map{it.nameAsString + '.'}.orElse('') +
                cu.types.first().nameAsString)
        new Unit(cu, javaClass, null)
    }

}
