package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformation
import com.aurea.methobase.meta.MetaInformationConsumer
import one.util.streamex.StreamEx

import java.util.concurrent.atomic.AtomicInteger

import static java.lang.System.exit

abstract class YamlProcessor<T extends MetaInformation> {

    private static final AtomicInteger PROGRESS = new AtomicInteger()

    private MetaInformationConsumer<T> consumer
    private int total

    YamlProcessor(MetaInformationConsumer<T> consumer) {
        this.consumer = consumer
    }

    YamlProcessor() {}

    void process(File yamlFileOrFolder) {
        consumer.accept(processYaml(yamlFileOrFolder))
    }

    StreamEx<T> processYaml(File yamlFileOrFolder) {
        if (!yamlFileOrFolder.exists()) {
            println "File not found: " + yamlFileOrFolder
            exit(-1)
        }

        List<File> ymlFiles = yamlFileOrFolder.directory ? yamlFileOrFolder.listFiles().toList() : [yamlFileOrFolder]
        total = ymlFiles.size()

        StreamEx.of(ymlFiles).parallel().flatMap{ ymlToComplexityLocEntries(it) }
    }

    StreamEx<T> ymlToComplexityLocEntries(File ymlFile) {

        StreamEx<T> metas = createRepository(ymlFile).all()
        println "Read ${PROGRESS.incrementAndGet()} / $total"
        metas
    }

    protected abstract YamlMetaInformationRepository<T> createRepository(File ymlFile)
}
