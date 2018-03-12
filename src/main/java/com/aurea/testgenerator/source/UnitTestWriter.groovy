package com.aurea.testgenerator.source

import com.aurea.testgenerator.config.ProjectConfiguration
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Path

@Component
@Log4j2
class UnitTestWriter {

    Path out
    boolean blank
    ExistingTestClassWriteStrategy existingTestClassWriteStrategy

    @Autowired
    UnitTestWriter(ProjectConfiguration cfg, List<ExistingTestClassWriteStrategy> strategies) {
        if (!cfg.blank) {
            this.out = cfg.outPath
        }
        this.blank = cfg.blank
        this.existingTestClassWriteStrategy = StreamEx.of(strategies)
                                                      .toMap({it.type}, {it})
                                                      .get(cfg.fileNameResolution)
    }

    void write(Unit unit) {
        if (blank) {
            log.info "Blank run, writing tests is disabled"
        } else {
            Path pathInOut = PathUtils.packageNameToPath(unit.cu.packageDeclaration.get().nameAsString)
            String fileName = unit.className + ".java"
            Path testFilePath = out.resolve(pathInOut).resolve(fileName)
            File testFile = testFilePath.toFile()
            if (!testFile.parentFile.exists()) {
                log.debug "Creating $testFile.parentFile"
                testFile.parentFile.mkdirs()
            }
            if (!testFile.exists()) {
                log.debug "Writing test: $testFile"
                testFile.write(unit.cu.toString())
            } else {
                existingTestClassWriteStrategy.write(testFile, unit)
            }
        }
    }
}
