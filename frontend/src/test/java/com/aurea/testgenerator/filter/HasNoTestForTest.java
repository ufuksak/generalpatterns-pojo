package com.aurea.testgenerator.filter;

import com.aurea.testgenerator.source.HasNoTestFor;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.aurea.testgenerator.PathUtils.nthParent;
import static org.assertj.core.api.Assertions.assertThat;

public class HasNoTestForTest {

    @Test
    public void testReturnsFalseWhenClassHasATestForIt() throws Exception {
        Path classFilePath = Paths.get(HasNoTestFor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path root = nthParent(classFilePath, 2);
        Path sourceRoot = root.resolve(Paths.get("src", "main", "java"));
        Path testRoot = root.resolve(Paths.get("src", "test", "java"));
        Path pathToJavaFile = sourceRoot.resolve(Paths.get("com", "aurea", "generator", "filter", "HasNoTestFor.java"));

        boolean result = new HasNoTestFor(testRoot, root.resolve(sourceRoot)).test(pathToJavaFile);

        assertThat(result).isFalse();
    }
}