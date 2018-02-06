package com.github.javaparser.adapters

import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.resolution.types.ResolvedPrimitiveType


class PrimitiveTypeAdapter {

    static Map<ResolvedPrimitiveType, PrimitiveType> MAPPING = new HashMap<>(ResolvedPrimitiveType.ALL.size())

    static {
        MAPPING[ResolvedPrimitiveType.BYTE] = PrimitiveType.byteType()
        MAPPING[ResolvedPrimitiveType.SHORT] = PrimitiveType.shortType()
        MAPPING[ResolvedPrimitiveType.CHAR] = PrimitiveType.charType()
        MAPPING[ResolvedPrimitiveType.INT] = PrimitiveType.intType()
        MAPPING[ResolvedPrimitiveType.LONG] = PrimitiveType.longType()
        MAPPING[ResolvedPrimitiveType.BOOLEAN] = PrimitiveType.booleanType()
        MAPPING[ResolvedPrimitiveType.FLOAT] = PrimitiveType.floatType()
        MAPPING[ResolvedPrimitiveType.DOUBLE] = PrimitiveType.doubleType()
    }

    static PrimitiveType adapt(ResolvedPrimitiveType resolved) {
        MAPPING[resolved]
    }
}
