package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MethodMetaInformation

class QueryRunner {

    static final Map<String, Closure<File>> QUERIES = [
            'c0-public-static': { File input -> new C0StaticBatch(input.parentFile.toPath().resolve('c0-public-static.yml')) },
            'primitive'       : { File input -> new PrimitiveBatch(input.parentFile.toPath().resolve('primitive.yml')) }

    ]

    static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please provide yaml file or folder and name of the query")
        }

        File yamlFileOrFolder = new File(args[0])
        String queryName = args[1]
        new YamlMethodProcessor(QUERIES[queryName].call(yamlFileOrFolder) as MetaInformationConsumer<MethodMetaInformation>).process(yamlFileOrFolder)
    }
}
