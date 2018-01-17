package com.aurea.testgenerator.config

import com.google.common.base.Splitter
import one.util.streamex.StreamEx
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MavenDependencyResolver {
    static final Logger logger = LogManager.getLogger(MavenDependencyResolver.class.getSimpleName());

    static final String CLASSPATH_MARKER = "[INFO] Dependencies classpath:"

    private List<String> jars
    private final String pomFile

    MavenDependencyResolver(String pomFile) {
        this.pomFile = pomFile
    }

    StreamEx<String> jars() {
        StreamEx.of(getJars())
    }

    private List<String> getJars() {
        if (null == jars) {
            String command = "mvn.bat -f $pomFile dependency:build-classpath -o"
            try {
                Process process = command.execute()
                String text = process.text
                List<String> lines = Splitter.on(System.lineSeparator()).splitToList(text)
                int classPathLine = lines.findIndexOf { it.contains(CLASSPATH_MARKER) }
                if (classPathLine < 0 || (classPathLine + 1) >= lines.size()) {
                    throw new MavenReaderException("Failed to find classpath dependency line output for $pomFile." +
                            " Output:${System.lineSeparator()}${getTail(text)}")
                }
                jars = Splitter.on(";").splitToList(lines.get(classPathLine).substring(CLASSPATH_MARKER.length() + 1))
            } catch (IOException ioe) {
                throw new MavenReaderException("Tried to execute '$command'", ioe)
            }
        }
        return jars
    }

    private String getTail(String text) {
        Iterable<String> lines = Splitter.on(System.lineSeparator()).split(text)
        lines.takeRight(10).join(System.lineSeparator())
    }

}
