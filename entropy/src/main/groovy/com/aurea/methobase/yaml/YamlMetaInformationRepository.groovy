package com.aurea.methobase.yaml

import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.methobase.meta.MetaInformation
import com.aurea.methobase.meta.MetaInformationRepository
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.meta.ModuleMetaInformation
import com.aurea.methobase.meta.ProjectMetaInformation
import com.esotericsoftware.yamlbeans.YamlConfig
import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import one.util.streamex.StreamEx

class YamlMetaInformationRepository<T extends MetaInformation> implements MetaInformationRepository<T> {

    private File readFrom
    private File writeTo
    private YamlConfig config
    private Class<T> elementClass

    static YamlMetaInformationRepository<MethodMetaInformation> createForMethods(File readFrom, File writeTo = null) {
        YamlConfig config = new YamlConfig()
        config.setClassTag("MethodMetaInformation", MethodMetaInformation)
        new YamlMetaInformationRepository<>(readFrom, writeTo, config, MethodMetaInformation.class)
    }

    static YamlMetaInformationRepository<ClassMetaInformation> createForClass(File readFrom, File writeTo = null) {
        YamlConfig config = new YamlConfig()
        config.setClassTag("ClassMetaInformation", ClassMetaInformation)
        new YamlMetaInformationRepository<>(readFrom, writeTo, config, ClassMetaInformation.class)
    }

    static YamlMetaInformationRepository<ProjectMetaInformation> createForProjects(File readFrom, File writeTo = null) {
        YamlConfig config = new YamlConfig()
        config.setClassTag("ProjectMetaInformation", ProjectMetaInformation)
        config.setClassTag("ModuleMetaInformation", ModuleMetaInformation)
        new YamlMetaInformationRepository<>(readFrom, writeTo, config, ProjectMetaInformation.class)
    }


    private YamlMetaInformationRepository(File readFrom, File writeTo, YamlConfig config, Class<T> elementClass) {
        this.readFrom = readFrom
        this.writeTo = writeTo
        this.config = config
        this.elementClass = elementClass
    }

    @Override
    StreamEx<T> all() {
        new FileReader(readFrom).withCloseable { fileReader ->
            YamlReader reader = new YamlReader(fileReader, config)
            List<T> metas = reader.read(List, elementClass)
            StreamEx.of(metas)
        }
    }

    void save(Collection<T> metas) {
        if (writeTo.exists()) {
            writeTo.delete()
        }
        if (!writeTo.parentFile.exists()) {
            writeTo.parentFile.mkdirs()
        }

        new FileWriter(writeTo).withCloseable { fileWriter ->
            YamlWriter writer = new YamlWriter(fileWriter, config)
            writer.write(metas)
            writer.close()
        }
    }
}
