package com.aurea.testgenerator.template.path

import com.aurea.testgenerator.pattern.ClassDescription

import java.nio.file.Path
import java.nio.file.Paths

class SingleModuleTestNameResolver implements TestNameResolver {
    @Override
    Path resolve(Path root, ClassDescription description) {
        return root.resolve(Paths.get(PathUtils.packageNameToFileName(description.packageName), description.className + "Test.java"))
    }
}
