package com.aurea.testgenerator.source;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface SourceFinder {

    List<Path> javaClasses(Path srcRoot) throws IOException;
}
