package com.aurea.testgenerator.source

import com.aurea.common.JavaClass
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.SimpleName
import groovy.util.logging.Log4j2
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

@Log4j2
@Component
class RenameClassWriteStrategy implements ExistingTestClassWriteStrategy {
    @Override
    void write(File existingTest, Unit testUnit) {
        File file = findFileWithSuitableName(existingTest.name, existingTest)
        Unit testUnitWithSuitableName = changeUnitName(testUnit, file.name)
        log.debug "Writing $file"
        file.write(testUnitWithSuitableName.cu.toString())
    }

    @Override
    FileNameConflictResolutionStrategyType getType() {
        FileNameConflictResolutionStrategyType.RENAME
    }


    private static File findFileWithSuitableName(String baseName, File current) {
        baseName = StringUtils.removeEndIgnoreCase(current.name, '.java')
        int index = 2
        while (current.exists()) {
            current = new File(current.parent, "${baseName}${index++}.java")
        }
        current
    }

    private static Unit changeUnitName(Unit current, String fileName) {
        assert !current.cu.getTypes().empty
        String newFullName = current.javaClass.package + '.' + fileName
        CompilationUnit newCu = current.cu.clone()
        newCu.getType(0).name = new SimpleName(fileName)
        new Unit(current.cu.clone(),
                new JavaClass(newFullName),
                current.modulePath
        )
    }
}

