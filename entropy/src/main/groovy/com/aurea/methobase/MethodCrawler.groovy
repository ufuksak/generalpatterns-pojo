package com.aurea.methobase

import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository

class MethodCrawler extends Crawler<MethodMetaInformation> {

    MethodCrawler(CrawlerConfiguration config) {
        super(config, YamlMetaInformationRepository.&createForMethods)
    }

    @Override
    protected List<MethodMetaInformation> toMetaInformations(Unit unit) {
        MethodVisitor visitor = new MethodVisitor()
        unit.cu.accept(visitor, unit)
        visitor.methodMetaInformations
    }

    static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please provide a new line separated file with paths to files")
        }

        List<File> javaFiles = []
        new File(args[0]).eachLine { line ->
            javaFiles << new File(line)
        }

        new MethodCrawler(CrawlerConfiguration
                .getDefault()
                .forProgressUse(MethodVisitor.METHOD_COUNTER)
                .usePrefix("method"))
                .run(javaFiles)
    }
}
