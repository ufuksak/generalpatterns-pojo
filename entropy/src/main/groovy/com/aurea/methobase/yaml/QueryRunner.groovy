package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformationRepository
import com.aurea.methobase.meta.MethodMetaInformation

class QueryRunner {

    static final Map<String, Closure<Query<MethodMetaInformation>>> QUERIES = [
            'c0-public-static': { MetaInformationRepository<MethodMetaInformation> repository -> new C0StaticBatch(repository) },
            'primitive'       : { MetaInformationRepository<MethodMetaInformation> repository -> new PrimitiveBatch(repository) }

    ]

    static final Map<String, String> BATCH_FILES = [
            'c0-public-static': 'c0-public-static.yml',
            'primitive'       : 'primitive.yml'
    ]

    static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please provide yaml file or folder and name of the query")
        }

        File yamlFileOrFolder = new File(args[0])
        String queryName = args[1]
        String saveFileName = BATCH_FILES[queryName]
        File saveFile = yamlFileOrFolder.parentFile.toPath().resolve(saveFileName).toFile()
        MetaInformationRepository<MethodMetaInformation> repository = YamlMetaInformationRepository.createForMethods(yamlFileOrFolder, saveFile)
        Query<MethodMetaInformation> query = QUERIES[queryName].call(repository)
        new YamlMethodProcessor(query).process(yamlFileOrFolder)
    }
}
