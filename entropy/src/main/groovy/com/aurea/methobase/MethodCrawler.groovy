package com.aurea.methobase

import com.aurea.methobase.meta.JavaParserFacadeFactory
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.meta.ProjectMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository

class MethodCrawler extends Crawler<MethodMetaInformation> {

    final JavaParserFacadeFactory factory
    MethodCrawler(CrawlerConfiguration config, Collection<ProjectMetaInformation> projectMetas) {
        super(config, YamlMetaInformationRepository.&createForMethods)
        factory = new JavaParserFacadeFactory(projectMetas)
    }

    @Override
    protected List<MethodMetaInformation> toMetaInformations(Unit unit) {
        MethodVisitor visitor = new MethodVisitor(factory)
        unit.cu.accept(visitor, unit)
        visitor.methodMetaInformations
    }

    static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please provide a new line separated file with paths to " +
                    "files as first argument and project metas yml as second")
        }

        List<File> javaFiles = []
        new File(args[0]).eachLine { line ->
            javaFiles << new File(line)
        }

        File projectMetasFile = new File(args[1])
        List<ProjectMetaInformation> projectMetas = YamlMetaInformationRepository.createForProjects(projectMetasFile).all().toList()

        new MethodCrawler(CrawlerConfiguration
                .getDefault()
                .forProgressUse(MethodVisitor.METHOD_COUNTER)
                .usePrefix("method"), projectMetas)
                .run(javaFiles)

        JavaParserFacadeFactory.UNSOLVED.each {
            println it
        }
    }
}
