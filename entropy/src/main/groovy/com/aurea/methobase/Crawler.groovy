package com.aurea.methobase

import com.aurea.methobase.meta.MetaInformation
import com.aurea.methobase.meta.MetaInformationRepository
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.jasongoodwin.monads.Try
import groovy.util.logging.Log4j2
import one.util.streamex.IntStreamEx
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Log4j2
abstract class Crawler<T extends MetaInformation> {

    CopyOnWriteArrayList<File> failedToParse = []
    final CrawlerConfiguration config
    final Closure<MetaInformationRepository<T>> repositoryFactory

    Crawler(CrawlerConfiguration config, Closure<MetaInformationRepository<T>> repositoryFactory) {
        this.config = config
        this.repositoryFactory = repositoryFactory
    }

    void run(List<File> javaFiles) {
        ScheduledExecutorService reportExecutor = Executors.newScheduledThreadPool(1)
        reportExecutor.schedule({
            println "Parsed ${config.progress} metas"
        }, config.reportIntervalSeconds, TimeUnit.SECONDS)

        List<File> files = IntStreamEx.range(0, javaFiles.size())
                                      .mapToObj { javaFiles.get it }
                                      .toList()

        List<Map.Entry<Integer, List<File>>> entries = StreamEx.ofSubLists(files, Math.min(config.numberOfFilesInChunk, files.size()))
                                                               .mapToEntry({ config.chunkProgress.incrementAndGet() }, { it })
                                                               .toList()
        StreamEx.of(entries).each { entry ->
            Try.ofFailable {
                int count = entry.key
                List<T> metas = crawl(entry.value, count)
                File saveTo = Paths.get(
                        config.saveToFolder.absolutePath,
                        "${config.metaPrefix}-${String.format("%04d", count)}.yml").toFile()
                repositoryFactory(null, saveTo).save(metas)
            }.onFailure {
                println "FAILED TO crawl chunk $entry.key"
            }.get()
        }

        if (!failedToParse.empty) {
            println "Failed to parse ${failedToParse.size()}/${javaFiles.size()} files"
        }
        reportExecutor.shutdown()
    }

    protected abstract List<T> toMetaInformations(Unit unit)

    List<T> crawl(List<File> javaFiles, int count) {
        println "Processing chunk $count / ${config.chunkProgress} with ${javaFiles.size()} files..."
        List<T> metas = []
        try {
            metas = StreamEx.of(javaFiles)
                            .parallel()
                            .map { toUnit it }
                            .flatMap { it.stream() }
                            .map { toMetaInformations it }
                            .flatMap { it.stream() }
                            .toList()
        } catch (Exception e) {
            log.error "Failed to crawl chunk $count", e
        } catch (StackOverflowError stackOverflowError) {
            log.error "Failed parsing $count with stack overflow", stackOverflowError
        }
        println "Finished chunk ${count} / ${config.chunkProgress}. It has ${metas.size()} metas."
        metas
    }

    Optional<Unit> toUnit(File file) {
        Try.<Unit> ofFailable {
            if (file.size() > config.fileSizeLimit) {
                failedToParse << file
                return Optional.empty()
            }
            CompilationUnit cu = JavaParser.parse(file)
            Path once = file.toPath().getRoot().relativize(file.toPath())
            Path root = once.getName(0)
            Path twice = root.relativize(once)
            new Unit(cu: cu,
                    className: file.name,
                    modulePath: twice)
        }.onFailure { failedToParse << file }.toOptional()
    }
}
                                                                    