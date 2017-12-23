package com.aurea.methobase.meta

import one.util.streamex.StreamEx


interface MetaInformationRepository<T extends MetaInformation> {
    StreamEx<T> all()

    void save(Collection<T> metas)
}