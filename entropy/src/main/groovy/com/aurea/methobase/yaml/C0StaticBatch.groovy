package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.Modifier

import java.nio.file.Path

class C0StaticBatch extends Query<MethodMetaInformation> {

    C0StaticBatch(Path saveTo) {
        super(YamlMetaInformationRepository.createForMethods(null, saveTo.toFile()), { MethodMetaInformation it ->
            it.isStatic && it.modifiers.contains(Modifier.PUBLIC)
        })
    }
}
