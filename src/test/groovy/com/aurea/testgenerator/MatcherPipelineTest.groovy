package com.aurea.testgenerator

import com.aurea.testgenerator.config.ProjectConfiguration
import com.aurea.testgenerator.generation.UnitTestCollector
import com.aurea.testgenerator.generation.UnitTestGenerator
import com.aurea.testgenerator.generation.UnitTestMergeEngine
import com.aurea.testgenerator.pattern.PatternMatchCollector
import com.aurea.testgenerator.pattern.PatternMatcher
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.PathUnitSource
import com.aurea.testgenerator.source.SourceFilters
import com.aurea.testgenerator.source.UnitSource
import com.aurea.testgenerator.source.UnitTestWriter
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat


abstract class MatcherPipelineTest extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    ProjectConfiguration cfg = new ProjectConfiguration()
    UnitSource source
    Pipeline pipeline
    UnitTestWriter unitTestWriter
    UnitTestMergeEngine mergeEngine
    UnitTestCollector unitTestCollector
    PatternMatchCollector patternMatchCollector

    void setup() {
        FileTreeBuilder tree = new FileTreeBuilder(folder.root)
        cfg.out = folder.newFolder("test-out").toPath()
        cfg.src = folder.newFolder("src").toPath()
        cfg.testSrc = folder.newFolder("test").toPath()

        source = new PathUnitSource(new JavaSourceFinder(cfg), cfg.src, SourceFilters.empty())
        patternMatchCollector = new PatternMatchCollector([matcher()])
        unitTestCollector = new UnitTestCollector([generator()])
        mergeEngine = new UnitTestMergeEngine()
        unitTestWriter = new UnitTestWriter(cfg)

        pipeline = new Pipeline(
                source,
                this.patternMatchCollector,
                this.unitTestCollector,
                SourceFilters.empty(),
                this.mergeEngine,
                this.unitTestWriter)
    }

    String onClassCodeExpect(String code, String expectedTest) {
        File testFile = new File(cfg.src.toFile().absolutePath + "/sample", 'Foo.java')
        testFile.parentFile.mkdirs()
        testFile.write """
        package sample;

        $code
        """

        pipeline.start()

        String resultingTest = cfg.out.resolve('sample').resolve('FooTest.java').toFile().text

        assertThat(resultingTest).isEqualToNormalizingWhitespace(expectedTest)
    }

    abstract PatternMatcher matcher()
    abstract UnitTestGenerator generator()
}
