package com.aurea.testgenerator.source;

import one.util.streamex.StreamEx;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceFinder {

    StreamEx<Path> javaClasses() throws IOException;
}
