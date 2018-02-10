package com.aurea.testgenerator.source

import com.aurea.testgenerator.config.ProjectConfiguration
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Path

@Component
@Log4j2
class UnitTestWriter {

    Path out

    void write(Unit unit) {
        Path pathInOut = PathUtils.packageNameToPath(unit.cu.packageDeclaration.get().nameAsString)
        String fileName = unit.className + ".java"
        Path writeTo = out.resolve(pathInOut).resolve(fileName)
        File testFile = writeTo.toFile()
        if (!testFile.parentFile.exists()) {
            log.debug "Creating $testFile.parentFile"
            testFile.parentFile.mkdirs()
        }
        if (testFile.exists()) {
            log.debug "$testFile existed before, deleting..."
            testFile.delete()
        }
        log.debug "Writing test: $testFile"
        testFile.write(unit.cu.toString())
    }

    @Autowired
    UnitTestWriter(ProjectConfiguration cfg) {
        out = cfg.out
    }
}
