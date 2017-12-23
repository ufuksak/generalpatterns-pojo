package com.aurea.methobase

import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import one.util.streamex.StreamEx

import java.util.concurrent.atomic.AtomicInteger

if (args.length != 2) {
    System.err.println("Please provide yaml file or folder as first argument and required complexity as second argument")
    System.exit(-1)
}

File yaml = new File(args[0])
int complexity = args[1] as int
File saveToDirectory = yaml.parentFile

List<File> yamls = yaml.directory ? yaml.listFiles().toList() : [yaml]

AtomicInteger progress = new AtomicInteger()
int total = yamls.size()

List<MethodMetaInformation> metasOfGivenComplexity = StreamEx.of(yamls)
                                                             .parallel().flatMap { yamlFile ->
    println "Parsing ${progress.incrementAndGet()} / $total"
    YamlMetaInformationRepository.createForMethods(yamlFile).all()
}.filter {!it.isAbstract}.filter { it -> it.cognitiveComplexity == complexity }.toList()

println "Found ${metasOfGivenComplexity.size()} methods with complexity $complexity"

int chunkSize = 100_000
AtomicInteger chunkCount = new AtomicInteger()

StreamEx.ofSubLists(metasOfGivenComplexity, chunkSize).each {
    File writeTo = new File("${saveToDirectory.absolutePath}/methods-with-complexity-${complexity}-${String.format("%04d", chunkCount.incrementAndGet())}.yml")
    println "Saving to $writeTo"
    YamlMetaInformationRepository.createForMethods(null, writeTo).save(it)
}




