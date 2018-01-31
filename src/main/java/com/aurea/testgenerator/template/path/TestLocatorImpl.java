package com.aurea.testgenerator.template.path;

import com.aurea.testgenerator.source.PathToUnitMapper;
import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.pattern.ClassDescription;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TestLocatorImpl implements TestLocator {

    private final TestNameResolver resolver;
    private final PathToUnitMapper testSourceMapper;

    public TestLocatorImpl(TestNameResolver resolver, PathToUnitMapper testSourceMapper) {
        this.resolver = resolver;
        this.testSourceMapper = testSourceMapper;
    }

    public Optional<Unit> get(ClassDescription description) {
        Path pathToTest = resolver.resolve(testSourceMapper.getRoot(), description);
        if (Files.exists(pathToTest)) {
            return testSourceMapper.apply(pathToTest);
        } else {
            return Optional.<Unit>empty();
        }
    }
}
