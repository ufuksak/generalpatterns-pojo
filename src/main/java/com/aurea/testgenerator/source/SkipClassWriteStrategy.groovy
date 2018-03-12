package com.aurea.testgenerator.source

import groovy.util.logging.Log4j2
import org.springframework.stereotype.Component


@Log4j2
@Component
class SkipClassWriteStrategy implements ExistingTestClassWriteStrategy {
    @Override
    void write(File existingTest, Unit testUnit) {
        if (log.debugEnabled) {
            log.debug "Skipping writing $testUnit, because file $existingTest already exists"
        }
    }

    @Override
    FileNameConflictResolutionStrategyType getType() {
        FileNameConflictResolutionStrategyType.SKIP
    }
}
