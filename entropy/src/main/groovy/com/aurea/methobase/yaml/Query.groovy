package com.aurea.methobase.yaml

import com.aurea.methobase.meta.MetaInformation
import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MetaInformationRepository
import one.util.streamex.StreamEx

import java.util.function.Predicate

abstract class Query<T extends MetaInformation> implements MetaInformationConsumer<T> {

    final MetaInformationRepository<T> repository
    final Predicate<T> filter

    Query(MetaInformationRepository<T> repository, Predicate<T> filter) {
        this.repository = repository
        this.filter = filter
    }

    @Override
    void accept(StreamEx<T> metas) {
        List<T> matches = metas.filter(filter).toList()
        println("Total ${matches.size()} found")
        repository.save(matches)
    }
}
