package com.aurea.testgenerator.source

import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component


@Log4j2
@Component
class OverrideClassWriteStrategy implements ExistingTestClassWriteStrategy {
    @Override
    void write(File existingTest, Unit testUnit) {
        log.debug "Deleting $existingTest"
        existingTest.delete()
        existingTest.write(testUnit.cu.toString())
    }

    @Override
    FileNameConflictResolutionStrategyType getType() {
        FileNameConflictResolutionStrategyType.OVERRIDE
    }
}
