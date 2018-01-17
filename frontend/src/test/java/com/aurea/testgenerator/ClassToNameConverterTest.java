package com.aurea.testgenerator;

import com.aurea.testgenerator.source.PathUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aurea.testgenerator.source.PathUtils.nthParent;
import static org.assertj.core.api.Assertions.assertThat;

public class ClassToNameConverterTest {

    @Test
    public void pathToPackageCorrectlyResolvesPackageNameByGivenRootPathAndPackagePath() throws IOException, URISyntaxException {
        Path thisFilePath = Paths.get(ClassToNameConverterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path root = nthParent(thisFilePath, 2).resolve(Paths.get("src", "main", "java"));
        Path source = Paths.get(PathUtils.packageNameToFileName("com.aurea.testgenerator"));
        Path fullSourcePath = root.resolve(source);

        Function<Path, String> toPackageName = PathUtils.newPathToPackage(root);
        List<String> packageNames = Files.walk(fullSourcePath).filter(Files::isDirectory).map(toPackageName).collect(Collectors.toList());

        assertThat(packageNames).allMatch(name -> name.contains("com.aurea.testgenerator"));
    }
}