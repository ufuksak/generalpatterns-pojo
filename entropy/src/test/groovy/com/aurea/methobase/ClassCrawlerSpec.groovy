package com.aurea.methobase

import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger


class ClassCrawlerSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    AtomicInteger fileNameCounter = new AtomicInteger()

    def "sanity check - crawler returns valid"() {
        given:
        File outputFolder = folder.newFolder("output")
        ClassCrawler crawler = new ClassCrawler(new CrawlerConfiguration(
                numberOfFilesInChunk: 1,
                reportIntervalSeconds: 1,
                chunkProgress: new AtomicInteger(),
                fileSizeLimit: 1_000_000,
                saveToFolder: outputFolder
        ))

        when:
        crawler.run([
                fileWithCode('''
class Foo {
    void foo() {}
}
''')
        ])

        then:
        File[] outputMetas = outputFolder.listFiles()
        outputMetas.length == 1
        List<ClassMetaInformation> metas = deserialize(outputMetas.first())
        metas.size() == 1
    }

    File fileWithCode(String code) {
        File file = folder.newFile("Foo${fileNameCounter.incrementAndGet()}.java")
        file.text = code
        file
    }

    List<ClassMetaInformation> deserialize(File yml) {
        YamlMetaInformationRepository.createForClass(yml).all().toList()
    }

}
