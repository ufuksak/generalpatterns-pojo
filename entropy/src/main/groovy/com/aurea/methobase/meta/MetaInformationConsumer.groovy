package com.aurea.methobase.meta

import one.util.streamex.StreamEx

import java.util.function.Consumer

@FunctionalInterface
interface MetaInformationConsumer<T extends MetaInformation> extends Consumer<StreamEx<T>> {
}
