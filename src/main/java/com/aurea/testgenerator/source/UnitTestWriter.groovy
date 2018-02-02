package com.aurea.testgenerator.source

import com.aurea.testgenerator.config.ProjectConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Path


@Component
@Log4j2
class UnitTestWriter {

    Path out

    @Autowired
    UnitTestWriter(ProjectConfiguration cfg) {
        out = cfg.out
    }

    void write(Map<Unit, CompilationUnit> testsByUnit) {
        log.info("Writing to $out")

        testsByUnit.values().forEach {
            Path pathInOut = PathUtils.packageNameToPath(it.packageDeclaration.get().nameAsString)
            ClassOrInterfaceDeclaration coid = it.findFirst(ClassOrInterfaceDeclaration).get()
            String fileName = coid.nameAsString + ".java"
            Path writeTo = out.resolve(pathInOut).resolve(fileName)
            File testFile = writeTo.toFile()
            if (!testFile.parentFile.exists()) {
                log.info "Creating $testFile.parentFile"
                testFile.parentFile.mkdirs()
            }
            if (testFile.exists()) {
                log.info "$testFile existed before, deleting..."
                testFile.delete()
            }
            log.info("Writing $testFile")
            testFile.write(it.toString())
        }
    }
}
