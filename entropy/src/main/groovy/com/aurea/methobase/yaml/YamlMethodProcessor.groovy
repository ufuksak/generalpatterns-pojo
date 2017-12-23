package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MethodMetaInformation

class YamlMethodProcessor extends YamlProcessor<MethodMetaInformation> {

    YamlMethodProcessor(MetaInformationConsumer<MethodMetaInformation> consumer) {
        super(consumer)
    }

    @Override
    protected YamlMetaInformationRepository<MethodMetaInformation> createRepository(File ymlFile) {
        YamlMetaInformationRepository.createForMethods(ymlFile)
    }
}
