package com.aurea.testgenerator.template;

import com.aurea.testgenerator.pattern.ClassDescription;
import com.aurea.testgenerator.pattern.PatternMatch;
import com.aurea.testgenerator.source.PathUtils;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class WithResourcesDecorator implements MatchCollector {

    private static final Logger logger = LogManager.getLogger(WithResourcesDecorator.class.getSimpleName());

    private final MatchCollector wrapped;
    private final List<Path> resources;
    private final Path target;
    private final Path sourceBase;

    public WithResourcesDecorator(MatchCollector wrapped, List<Path> resources, Path target) {
        this.wrapped = wrapped;
        this.target = target;
        try {
            this.sourceBase = new File(this.getClass().getClassLoader().getResource(
                    PathUtils.packageNameToFileName(this.getClass().getPackage().getName()))
                    .toURI()).toPath();
            this.resources = resources;
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to find resource folder for class " + this.getClass().getName());
        }
    }

    @Override
    public void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        wrapped.collect(classesToMatches);
        File targetDir = target.toFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        resources.forEach(r -> {
            Path sourcePath = sourceBase.resolve(r);
            String name = r.toFile().getName();
            Path targetPath = target.resolve(name);
            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Failed to copy {} -> {}", sourcePath, targetPath, e);
            }
        });
    }

    @Override
    public String toString() {
        return "+" + StreamEx.of(resources).joining(System.lineSeparator());
    }
}
