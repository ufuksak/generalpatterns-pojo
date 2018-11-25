package com.aurea.testgenerator.source

import com.google.googlejavaformat.java.Formatter
import com.google.googlejavaformat.java.ImportOrderer
import com.google.googlejavaformat.java.RemoveUnusedImports
import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component

@Log4j2
@Component
class OverrideClassWriteStrategy implements ExistingTestClassWriteStrategy {
    @Override
    void write(File existingTest, Unit testUnit) {
        log.debug "Deleting $existingTest"
        existingTest.delete()
        String fileContents = testUnit.cu.toString()
        fileContents = RemoveUnusedImports.removeUnusedImports(fileContents)
        fileContents = ImportOrderer.reorderImports(fileContents)
        fileContents = new Formatter().formatSource(fileContents)
        existingTest.write(fileContents)
    }

    @Override
    FileNameConflictResolutionStrategyType getType() {
        FileNameConflictResolutionStrategyType.OVERRIDE
    }
}
