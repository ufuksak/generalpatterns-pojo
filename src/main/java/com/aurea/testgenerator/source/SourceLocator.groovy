package com.aurea.testgenerator.source

import com.aurea.testgenerator.config.ProjectConfiguration
import com.aurea.testgenerator.source.structure.Module
import com.aurea.testgenerator.source.structure.Project
import com.esotericsoftware.yamlbeans.YamlWriter
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.CompilationUnit
import groovy.util.logging.Log4j2

import java.nio.file.Path

@Log4j2
class SourceLocator {

    static void main(String[] args) {
        List<File> unstructuredProjects = []
        new File("c:/crossover/only-java").eachFile {
            if (it.directory) {
                if (!it.toPath().resolve('project-structure.yml').toFile().exists()) {
                    unstructuredProjects << it
                }
            }
        }
        unstructuredProjects.parallelStream().each {
            write it.absolutePath
        }
    }

    private static void write(String projectName) {
        ProjectConfiguration cfg = new ProjectConfiguration(
                src: projectName
        )
        Set<Path> rootPaths = []
        new JavaSourceFinder(cfg).javaClasses().each { javaClassPath ->
            try {
                CompilationUnit cu = JavaParser.parse(javaClassPath)
                String packageName = cu.packageDeclaration.map { it.nameAsString }.orElse("")
                if (!packageName) {
                    log.error "No package declaration in $javaClassPath"
                } else {
                    Path packagePath = PathUtils.packageNameToPath(packageName)
                    Path expectedPackagePath = javaClassPath.subpath(0, javaClassPath.nameCount - 1)
                    if (!expectedPackagePath.endsWith(packagePath)) {
                        log.error "Inconsistent java class path and package declaration: $expectedPackagePath does not end with $packagePath"
                    } else {
                        int nameToSubstract = packagePath.nameCount
                        Path rootPath = expectedPackagePath.subpath(0, javaClassPath.nameCount - nameToSubstract - 1)
                        rootPaths << rootPath
                    }
                }

            } catch (ParseProblemException ppe) {
                log.error "Failed to parse $javaClassPath"
            }
        }

        Map<Path, Path> testSrcs = new HashMap<>()
        Map<Path, Path> testBeforeLastSrcs = new HashMap<>()
        Set<Path> srcs = []
        List<String> testDirectories = ['test', 'tests', 'integrationTest', 'Test', 'itest', 'testsrc', 'src_test', 'unittests']
        rootPaths.each {
            String lastName = it.getName(it.nameCount - 1)
            String beforeLastname = it.getName(it.nameCount - 2)
            if (lastName in testDirectories) {
                testSrcs.put(it.subpath(0, it.nameCount - 1), it)
            } else if (beforeLastname in testDirectories) {
                testBeforeLastSrcs.put(it.subpath(0, it.nameCount - 2), it)
            } else {
                srcs << it
            }
        }

        List<Module> modules = srcs.collect {
            Path commonPathWithTests = it.subpath(0, it.nameCount - 1)
            Path commonPathWithBeforeLastTests = it.subpath(0, it.nameCount - 2)
            Path testPath = null
            if (testSrcs.containsKey(commonPathWithTests)) {
                testPath = testSrcs[commonPathWithTests]
            } else if (testBeforeLastSrcs.containsKey(commonPathWithBeforeLastTests)) {
                testPath = testBeforeLastSrcs[commonPathWithBeforeLastTests]
            }

            return new Module(it.toString(), testPath.toString())
        }

        Project project = new Project(modules)
        YamlWriter writer = new YamlWriter(new FileWriter("${projectName}/project-structure.yml"))
        writer.getConfig().setClassTag('Project', Project)
        writer.getConfig().setClassTag('Module', Module)
        writer.write(project)
        writer.close()
    }
}
