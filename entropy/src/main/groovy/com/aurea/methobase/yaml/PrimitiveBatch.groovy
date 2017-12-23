package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MethodMetaInformation

import java.nio.file.Path

class PrimitiveBatch extends Query<MethodMetaInformation> {

    PrimitiveBatch(Path saveTo) {
        super(YamlMetaInformationRepository.createForMethods(null, saveTo.toFile()), { MethodMetaInformation it ->
            it.referencedTypes.stream().allMatch { it in ALLOWED_TYPES }
        })
    }

    static final Set<String> ALLOWED_TYPES = [
            'int',
            'Integer',
            'java.lang.Integer',

            'short',
            'Short',
            'java.lang.Short',

            'byte',
            'Byte',
            'java.lang.Byte',

            'boolean',
            'Boolean',
            'java.lang.Boolean',

            'float',
            'Float',
            'java.lang.Float',

            'double',
            'Double',
            'java.lang.Double',

            'long',
            'Long',
            'java.lang.Long',

            'char',
            'Character',
            'java.lang.Character',

            'String',
            'java.lang.String'
    ]

}
