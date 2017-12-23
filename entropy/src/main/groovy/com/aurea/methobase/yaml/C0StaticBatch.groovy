package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformationRepository
import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.Modifier

class C0StaticBatch extends Query<MethodMetaInformation> {

    C0StaticBatch(MetaInformationRepository<MethodMetaInformation> repository) {
        super(repository, { MethodMetaInformation it ->
            it.isStatic && it.modifiers.contains(Modifier.PUBLIC)
        })
    }
}
