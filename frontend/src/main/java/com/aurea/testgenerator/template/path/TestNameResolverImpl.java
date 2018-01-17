package com.aurea.testgenerator.template.path;

import com.aurea.testgenerator.source.PathUtils;
import com.aurea.testgenerator.pattern.ClassDescription;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestNameResolverImpl implements TestNameResolver {
    @Override
    public Path resolve(Path root, ClassDescription description) {
        String packagePath = PathUtils.packageNameToFileName(description.getPackageName());
        return root.resolve(description.getModulePath().resolve(Paths.get(packagePath, description.getClassName() +
                "Test.java")));
    }
}
