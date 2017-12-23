package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformationRepository
import com.aurea.methobase.meta.MethodMetaInformation

class PrimitiveBatch extends Query<MethodMetaInformation> {

    PrimitiveBatch(MetaInformationRepository<MethodMetaInformation> repository) {
        super(repository, { MethodMetaInformation it ->
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
