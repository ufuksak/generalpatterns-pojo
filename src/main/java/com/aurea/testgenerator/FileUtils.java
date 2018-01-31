package com.aurea.testgenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class.getSimpleName());

    public static void recreateDirectory(Path directory) {
        try {
            if (Files.isDirectory(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            Files.createDirectory(directory);
        } catch (IOException e) {
            logger.error("Failed to clean up: " + directory, e);
        }
    }

    public static void copyDirectory(Path source, Path target) {
        try {
            Files.walkFileTree(source, new CopyDirectoryVisitor(source, target, StandardCopyOption.REPLACE_EXISTING));
        } catch (IOException e) {
            logger.error("Failed to copy {} -> {}", source, target);
        }
    }

    public static FileWriter getFileWriter(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return new FileWriter(path.toFile());
    }

    public static class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path fromPath;
        private final Path toPath;
        private final CopyOption copyOption;

        public CopyDirectoryVisitor(Path fromPath, Path toPath, CopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
