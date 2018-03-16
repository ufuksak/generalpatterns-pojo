package com.aurea.testgenerator.source


interface ExistingTestClassWriteStrategy {

    void write(File existingTest, Unit testUnit)

    FileNameConflictResolutionStrategyType getType()
}
