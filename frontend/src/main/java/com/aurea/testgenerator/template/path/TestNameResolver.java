package com.aurea.testgenerator.template.path;

import com.aurea.testgenerator.pattern.ClassDescription;

import java.nio.file.Path;

public interface TestNameResolver {

    Path resolve(Path root, ClassDescription description);
}
