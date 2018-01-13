package com.aurea.methobase

import com.aurea.methobase.meta.ModuleMetaInformation
import com.aurea.methobase.meta.ProjectMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import com.aurea.testgenerator.source.PathUtils
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.PackageDeclaration
import com.jasongoodwin.monads.Try
import groovy.io.FileType
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class ProjectCrawler {
    private static CrawlerConfiguration config
    private static CopyOnWriteArrayList<File> failedToParse = []


    static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please provide root folder to projects dir")
        }

        File root = new File(args[0])
        config = CrawlerConfiguration
                .getDefault()
                .saveToFolder(root)
                .usePrefix("project")


        List<File> dirs = []
        root.eachDir {
            dirs << it
        }

        int total = dirs.size()
        AtomicInteger progress = new AtomicInteger()

        Map<File, Set<Path>> projectsToModules = StreamEx.of(dirs).parallel()
            .mapToEntry {
                Set<Path> modules = []
                it.eachFileRecurse FileType.FILES, { File javaFile ->
                    toModulePath(javaFile).ifPresent {
                        modules << it
                    }

                }
                println "Parsed ${progress.incrementAndGet()} / $total projects"
                modules
            }.toMap()

        Collection<ProjectMetaInformation> projectMetas = EntryStream.of(projectsToModules).mapKeyValue{ projectPath, modulePaths ->
            new ProjectMetaInformation(
                    filePath: projectPath,
                    modules: StreamEx.of(modulePaths).map { modulePath ->
                        Path filePath = Paths.get("")
                        try {
                            filePath = modulePath.subpath(2, modulePath.nameCount)
                        } catch (IllegalArgumentException iae) {}
                        new ModuleMetaInformation(filePath: filePath)
                    }.toList()
            )
        }.toList()

        YamlMetaInformationRepository repo = YamlMetaInformationRepository
                .createForProjects(null, config.saveToFolder.toPath().resolve("project-metas.yml").toFile())
        repo.save(projectMetas)
    }

    static Optional<Path> toModulePath(File javaFile) {
        try {
            Optional<Unit> maybeUnit = toUnit(javaFile)
            Optional<PackageDeclaration> pd = maybeUnit.flatMap { it.cu.findFirst(PackageDeclaration) }
            String packageName = pd.map { it.nameAsString }.orElse("")
            if (packageName) {
                Path packagePath = PathUtils.packageNameToPath(packageName)
                Path javaClassPath = packagePath.resolve(javaFile.name)
                Path javaFilePath = javaFile.toPath()
                if (javaFilePath.endsWith(javaClassPath)) {
                    int until = javaFilePath.nameCount - javaClassPath.nameCount
                    return Optional.of(javaFilePath.subpath(0, until))
                } else {
                    return Optional.empty()
                }
            }
        } catch (Exception e) {
            println "Failed to crawl in $javaFile"
        }
        return Optional.empty()
    }

    static Optional<Unit> toUnit(File file) {
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
