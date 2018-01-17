package com.aurea.testgenerator.template;

import com.aurea.testgenerator.FileUtils;
import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.source.PathUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WithResourcesDirectoryDecorator implements MatchCollector {

    private final MatchCollector wrapped;
    private final Path source;
    private final Path target;

    public WithResourcesDirectoryDecorator(MatchCollector wrapped, Path pathInResources, Path target) {
        this.wrapped = wrapped;
        try {
            this.source = new File(this.getClass().getClassLoader().getResource(
                    PathUtils.packageNameToFileName(this.getClass().getPackage().getName()))
                    .toURI()).toPath().resolve(pathInResources);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to find resource folder for class " + this.getClass().getName());
        }

        this.target = target;
    }

    @Override
    public void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        wrapped.collect(classesToMatches);
        FileUtils.copyDirectory(source, target);
    }

    @Override
    public String toString() {
        return "[" + wrapped.toString() + "] and Resources in [" + target + "]";
    }
}
