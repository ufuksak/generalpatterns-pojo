package com.aurea.source

import groovy.transform.Memoized
import one.util.streamex.StreamEx

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream


class JavaSource {

    @Memoized
    StreamEx<Path> javaFilePaths(Path srcRoot) throws IOException {
        StreamEx.of(Files.walk(srcRoot))
                .filter { Files.isDirectory(it) }
                .parallel()
                .flatMap { toClasses(it) }
    }

    private static Stream<Path> toClasses(Path packageDirectory) {
        try {
            return Files.walk(packageDirectory, 1)
                        .filter { Files.isRegularFile(it) && it.toFile().getName().endsWith(".java") }
        } catch (IOException e) {
            e.printStackTrace()
        }
        Stream.empty()
    }
}
