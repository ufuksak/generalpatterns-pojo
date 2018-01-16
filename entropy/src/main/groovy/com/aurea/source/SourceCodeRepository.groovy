package com.aurea.source

import com.aurea.methobase.Unit
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.jasongoodwin.monads.Try
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

import java.nio.file.Path
import java.util.function.Predicate

@Log4j2
class SourceCodeRepository {

    List<Path> javaFilePaths

    SourceCodeRepository(File dir) {
        Objects.requireNonNull(dir, "Should not be given null directory")
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory $dir doesn't exist")
        }
        javaFilePaths = new JavaSource().javaFilePaths(dir.toPath()).toList()
    }

    StreamEx<MethodDeclaration> methods(Predicate<MethodDeclaration> isTestable,
                                        Predicate<TypeDeclaration> isTypeTestable = { true }) {
        StreamEx<CompilationUnit> cus = StreamEx.of(javaFilePaths).flatMap { javaFilePath ->
            Try.ofFailable {
                Optional.of(JavaParser.parse(javaFilePath))
            }.onFailure { e ->
                log.error("Failed to parse $javaFilePath: $e")
            }.orElse(Optional.empty()).stream()
        }
        cus.map { new Unit(cu: it, )}
    }
}
