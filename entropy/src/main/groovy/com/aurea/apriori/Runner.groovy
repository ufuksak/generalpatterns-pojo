package com.aurea.apriori

import com.aurea.methobase.AprioriQuantifier
import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMethodProcessor
import de.mrapp.apriori.Apriori
import de.mrapp.apriori.ItemSet
import de.mrapp.apriori.Output
import de.mrapp.apriori.Transaction
import one.util.streamex.StreamEx

import static java.lang.System.exit

class Runner implements MetaInformationConsumer<MethodMetaInformation> {
    private Set<ItemSet<NamedItem>> aprioriOutput

    static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            println 'Required parameters: yaml-file'
            exit(-1)
        }

        def runner = new Runner()
        new YamlMethodProcessor(runner).process(new File(args.first()))

        def aprioriQuantifier = new AprioriQuantifier(runner.aprioriOutput)
        new YamlMethodProcessor(aprioriQuantifier).process(new File(args.first()))
    }

    private static Set<ItemSet<NamedItem>> runApriori(StreamEx<MethodMetaInformation> metas) {

//        int count = 5
//        Apriori<NamedItem> apriori = new Apriori.Builder<NamedItem>(count).supportDelta(0.1).maxSupport(1.0).minSupport(0.0).create()

        double minSupport = 0.001
        Apriori<NamedItem> apriori = new Apriori.Builder<NamedItem>(minSupport).create()

        Iterable<Transaction<NamedItem>> iterable = toTransactions(metas)
        Output<NamedItem> output = apriori.execute(iterable)
        output.getFrequentItemSets().findAll { it.size() > 1 }
    }

    static Iterable<Transaction<NamedItem>> toTransactions(StreamEx<MethodMetaInformation> metas) {
        metas.map { toTransaction(it.referencedTypes) }.toList()
    }

    static Transaction<NamedItem> toTransaction(Set<String> referencedTypes) {
        new NamedTransaction(referencedTypes.findAll { it && !(it in ['?', 'void'])}.collect { new NamedItem(it) })
    }

    @Override
    void accept(StreamEx<MethodMetaInformation> metas) {
        aprioriOutput = runApriori(metas)
        println aprioriOutput
    }
}
