package com.aurea.testgenerator.source

import com.aurea.testgenerator.source.structure.Module
import com.aurea.testgenerator.source.structure.Project
import com.esotericsoftware.yamlbeans.YamlReader
import groovy.util.logging.Log4j2

import java.nio.file.Path
import java.nio.file.Paths


@Log4j2
class SanityChecker {

    static void main(String[] args) {
        new File("D:/trilogy-group-only-java").eachDir { dir ->
            if (dir.name == 'acr-aurea-ace-butterfly') {
                YamlReader reader = new YamlReader(new FileReader(dir.toPath().resolve('project-structure.yml').toFile()))
                reader.getConfig().setClassTag('Project', Project)
                reader.getConfig().setClassTag('Module', Module)
                Project project = reader.read(Project)
                reader.close()
                if (project.modules) {
                    log.info "Generating for project $dir"
                    project.modules.each {
                        String src = it.src.replace('trilogy-group-java', 'D:/trilogy-group-only-java')
                        String projectSrc = "--project.src=${src}"
                        Path pathToSrc = Paths.get(projectSrc)
                        Path pathToTest = pathToSrc.subpath(0, pathToSrc.nameCount - 1).resolve('test')
                        String projectOut = "--project.out=$pathToTest"
                        ProcessBuilder builder = new ProcessBuilder(
                                "java", "-jar", "D:/crossover/repos/BigCodeTestGenerator/build/libs/big-code-1.2.1.jar",
                                projectSrc, projectOut, "--spring.profiles.active=open-pojo,class-level")
                        builder.inheritIO()
                        log.info """
----------------------------------------------------------------------------------------------------------------------------------------------------

${builder.command().join(" ")} 

----------------------------------------------------------------------------------------------------------------------------------------------------

                    """
                        Process started = builder.start()
                        started.waitFor()
                        println "Finished generation for $it: ${started.exitValue()}"
                    }
                }
            }
        }
    }
}
