package com.aurea.methobase.meta

import com.github.javaparser.ast.Node
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList

class JavaParserFacadeFactory {

    static final List<String> UNSOLVED = new CopyOnWriteArrayList<>()

    final Map<String, JavaParserFacade> facadesMap

    final static JavaParserFacade DEFAULT_FACADE = JavaParserFacade.get(new ReflectionTypeSolver())

    JavaParserFacadeFactory(Collection<ProjectMetaInformation> projectMetas) {
        facadesMap = StreamEx.of(projectMetas).toMap({
            toProjectName it
        }, { projectMeta ->
            CombinedTypeSolver solver = new CombinedTypeSolver()
            StreamEx.of(projectMeta.modules).map { new JavaParserTypeSolver(Paths.get(projectMeta.filePath, it.filePath).toFile()) }.each { solver.add(it) }
            solver.add(new ReflectionTypeSolver())
            JavaParserFacade.get(solver)
        })
    }

    JavaParserFacade fromProjectMetaInformation(ProjectMetaInformation projectMeta) {
        fromProjectName(toProjectName(projectMeta))
    }

    JavaParserFacade fromProjectName(String name) {
        facadesMap.getOrDefault(name, DEFAULT_FACADE)
    }

    static String toProjectName(ProjectMetaInformation projectMeta) {
        Path projectPath = Paths.get(projectMeta.filePath)
        String name = projectPath.getName(Math.max(1, projectPath.getNameCount() - 1))
        name
    }

    static void reportAsUnsolved(Node n) {
        n.findCompilationUnit().ifPresent { cu ->
            Path pathTo = cu.storage.get().path
            UNSOLVED << "Failed to solve $n in $pathTo"
        }
    }
}
