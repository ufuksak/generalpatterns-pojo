package com.aurea.testgenerator.source

import groovy.transform.Memoized
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function
import java.util.stream.Stream

@Component
@Log4j2
class JavaSourceFinder implements SourceFinder {

    @Memoized
    List<Path> javaClasses(Path srcRoot) throws IOException {
        StreamEx.of(Files.walk(srcRoot))
                .filter { Files.isDirectory(it) }
                .parallel()
                .map { toClasses(it) }
                .flatMap(Function.identity())
                .toList()
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
