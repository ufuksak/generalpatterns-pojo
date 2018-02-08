package com.aurea.testgenerator.source;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import static com.aurea.common.ParsingUtils.parseJavaClassName;

public class PathToUnitMapper implements Function<Path, Optional<Unit>> {

    private static final Logger logger = LogManager.getLogger(PathToUnitMapper.class.getSimpleName());

    private final Path root;

    public PathToUnitMapper(Path root) {
        this.root = root;
    }

    public Path getRoot() {
        return root;
    }

    @Override
    public Optional<Unit> apply(Path path) {
        try {
            if (path.toString().isEmpty()) {
                return Optional.empty();
            }
            CompilationUnit cu = JavaParser.parse(path.toFile(), Charset.defaultCharset());
            String simpleFileName = path.getFileName().toString();
            String className = parseJavaClassName(path);
            Path fullPathToFile = root.relativize(path);
            return cu.getPackageDeclaration().map(pd -> {
                Path pathToFileInPackage = Paths.get(PathUtils.packageNameToFileName(pd.getNameAsString()), simpleFileName);
                Path modulePath = createModulePath(fullPathToFile, pathToFileInPackage);

                return new Unit(cu, className, pd.getNameAsString(), modulePath);
            });
        } catch (NullPointerException npe) {
            logger.error("Inexplicable on: '" + path + "'", npe);
        } catch (FileNotFoundException e) {
            logger.error("File not found: '" + path + "'", e);
        } catch (ParseProblemException ppe) {
            logger.error("Failed to parse: '" + path + "'");
        }

        return Optional.empty();
    }

    private static Path createModulePath(Path fullPath, Path pathInPackage) {
        int nameCount = pathInPackage.getNameCount();
        if (nameCount == fullPath.getNameCount()) {
            return fullPath.subpath(0, fullPath.getNameCount() - 1);
        }
        try {
            return fullPath.subpath(0, fullPath.getNameCount() - nameCount);
        } catch (IllegalArgumentException iae) {
            return Paths.get("");
        }
    }
}
