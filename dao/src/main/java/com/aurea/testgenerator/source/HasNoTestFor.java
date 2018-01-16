package com.aurea.testgenerator.source;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class HasNoTestFor implements Predicate<Path> {

    private final Path testRoot;
    private final Path sourceRoot;

    public HasNoTestFor(Path testRoot, Path sourceRoot) {
        this.testRoot = testRoot;
        this.sourceRoot = sourceRoot;
    }

    @Override
    public boolean test(Path pathToJavaFile) {
        Path relativeToSourceRoot = pathToJavaFile.relativize(sourceRoot);
        Path testFilePath = testRoot.resolve(relativeToSourceRoot);
        return !Files.exists(testFilePath);
    }
}
