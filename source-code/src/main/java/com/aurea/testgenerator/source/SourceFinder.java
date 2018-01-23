package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface SourceFinder {

    StreamEx<Path> javaClasses() throws IOException;
}
