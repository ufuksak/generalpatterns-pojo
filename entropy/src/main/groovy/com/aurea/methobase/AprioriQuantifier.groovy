package com.aurea.methobase

import com.aurea.apriori.NamedItem
import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MethodMetaInformation
import de.mrapp.apriori.ItemSet
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx

import java.util.stream.Collectors

import static java.lang.System.lineSeparator

class AprioriQuantifier implements MetaInformationConsumer<MethodMetaInformation> {

    private Set<Set<String>> aprioriOutput

    AprioriQuantifier(Set<ItemSet<NamedItem>> aprioriOutput) {
        this.aprioriOutput = StreamEx.of(aprioriOutput).map { it.name }.toSet()
    }

    @Override
    void accept(StreamEx<MethodMetaInformation> methodMetaInformations) {
        Map<Set<String>, Integer> results = methodMetaInformations
                .filter { !it.isAbstract }
                .groupingBy({ findItemSet(it.referencedTypes) }, Collectors.summingInt { it.locs })
        def total = results.values().sum() as long
        println "Methods share (coverage-wise) for following references"
        EntryStream.of(results).sortedByInt { -it.value }.each {
            print "${it.key}: "
            printf("%.1f%%${lineSeparator()}", 100.0 * it.value / total)
        }
    }

    Set<String> findItemSet(Set<String> referencedTypes) {
        aprioriOutput.find { referencedTypes.containsAll(it) } ?: []
    }

}
