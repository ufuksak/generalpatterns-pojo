package com.aurea.methobase

import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import com.aurea.testgenerator.source.JavaSourceFinder
import com.aurea.testgenerator.source.SourceFilter
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.nio.file.Paths

class ClassCrawler extends Crawler<ClassMetaInformation> {

    //Seems to be a Groovy bug, addressing SourceFilter.empty() gives errors
    final static SourceFilter NO_FILTER = new SourceFilter() {
        @Override
        boolean test(Path path) {
            return true
        }
    }

    static Map<Path, SymbolSolver> SOLVERS

    ClassCrawler(CrawlerConfiguration config) {
        super(config, YamlMetaInformationRepository.&createForMethods)
    }

    @Override
    protected List<ClassMetaInformation> toMetaInformations(Unit unit) {
        SymbolSolver solver = SOLVERS[unit.modulePath.subpath(0, 1)]
        TypeVisitor visitor = new TypeVisitor(solver)
        unit.cu.accept(visitor, unit)
        visitor.metas
    }

    static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please provide a new line separated file with paths to files")
        }

        File root = new File(args[0])
        Set<Path> projects = []
        root.eachLine { line ->
            Path p = Paths.get(line)
            projects << p.root.resolve(p.subpath(0, 2))
        }

        List<File> javaFiles = []
        root.eachLine { line ->
            javaFiles << new File(line)
        }

        SOLVERS = StreamEx.of(projects).toMap({it.subpath(1, 2)}, {
            JavaSourceFinder finder = new JavaSourceFinder()
            new SymbolSolver(finder, it, NO_FILTER)
        })

        ClassCrawler crawler = new ClassCrawler(CrawlerConfiguration
                .getDefault()
                .saveToFolder(root.parentFile)
                .usePrefix("class")
                .forProgressUse(TypeVisitor.COUNTER))

        crawler.run(javaFiles)
    }
}
