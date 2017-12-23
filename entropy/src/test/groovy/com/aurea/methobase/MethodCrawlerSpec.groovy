package com.aurea.methobase

import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

class MethodCrawlerSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    AtomicInteger fileNameCounter = new AtomicInteger()

    def "sanity check - crawler returns valid .yml"() {
        given:
        File outputFolder = folder.newFolder("output")
        MethodCrawler crawler = new MethodCrawler(new CrawlerConfiguration(
                numberOfFilesInChunk: 1,
                reportIntervalSeconds: 1,
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
        List<MethodMetaInformation> metas = deserialize(outputMetas.first())
        metas.size() == 1
        metas[0].cognitiveComplexity == 0
        metas[0].name == 'foo'
    }

    def "does chunking"() {
        given:
        File outputFolder = folder.newFolder("output")
        MethodCrawler crawler = new MethodCrawler(new CrawlerConfiguration(
                numberOfFilesInChunk: 1,
                reportIntervalSeconds: 1,
                fileSizeLimit: 1_000_000,
                saveToFolder: outputFolder
        ))

        when:
        crawler.run([
                fileWithCode('''
class Foo {
    void foo() {}
}
'''), fileWithCode('''
class Bar {
    void bar() {}
}
''')
        ])

        then:
        File[] outputMetas = outputFolder.listFiles()
        outputMetas.length == 2
        List<MethodMetaInformation> firstChunkMetas = deserialize(outputMetas[0])
        firstChunkMetas.size() == 1

        List<MethodMetaInformation> secondChunkMetas = deserialize(outputMetas[1])
        secondChunkMetas.size() == 1
    }

    File fileWithCode(String code) {
        File file = folder.newFile("Foo${fileNameCounter.incrementAndGet()}.java")
        file.text = code
        file
    }

    List<MethodMetaInformation> deserialize(File yml) {
        YamlMetaInformationRepository.createForMethods(yml).all().toList()
    }
}
