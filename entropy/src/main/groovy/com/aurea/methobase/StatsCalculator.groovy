package com.aurea.methobase

import com.aurea.methobase.meta.MetaInformationConsumer
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMethodProcessor
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx

import java.util.stream.Collectors

import static java.lang.System.exit
import static java.lang.System.lineSeparator

/**
 * This script goes through yaml list of MethodMetaInformation to calculate both
 * accumulating total of statements in methods not exceeding complexity values and frequency of it.
 */
class StatsCalculator implements MetaInformationConsumer<MethodMetaInformation> {

    private static String VALUE_SEPARATOR = ';'
    private static String CSV_FILE = 'distribution.csv'
    private static Integer COMPLEXITY_LIMIT = 30


    private File yamlFileOrFolder

    StatsCalculator(File yamlFileOrFolder) {
        this.yamlFileOrFolder = yamlFileOrFolder
    }

    @Override
    void accept(StreamEx<MethodMetaInformation> methodMetaInformations) {
        Map<Integer, Integer> complexityCount = methodMetaInformations
                .filter{ !it.isAbstract }
                .groupingBy({it.cognitiveComplexity}, Collectors.summingInt { it.locs })
        def current = 0
        def total = complexityCount.values().sum() as double
        Map<Integer, Integer> cumulativeDistribution = [:]
        EntryStream.of(complexityCount)
                   .parallel()
                   .filterKeys { it <= COMPLEXITY_LIMIT }
                   .sortedByInt { it.key }.each {
            current += it.value
            cumulativeDistribution[it.key] = current
        }

        saveDistributionToFile(cumulativeDistribution, total)
    }

    private void saveDistributionToFile(Map<Integer, Integer> cumulativeDistribution, double total) {
        new FileWriter(yamlFileOrFolder.absoluteFile.parent + File.separator + CSV_FILE).withCloseable { fileWriter ->
            fileWriter.write("complexity${VALUE_SEPARATOR}frequency${VALUE_SEPARATOR}locs${lineSeparator()}")
            cumulativeDistribution.each {
                fileWriter.append(it.key + VALUE_SEPARATOR + it.value / total + VALUE_SEPARATOR + it.value + lineSeparator())
            }
        }
    }

    static void main(String[] args) {

        if (args.size() != 1) {
            println 'Required parameters: yaml-file'
            exit(-1)
        }

        def statsCalculator = new StatsCalculator(new File(args.first()))
        new YamlMethodProcessor(statsCalculator).process(statsCalculator.yamlFileOrFolder)
    }
}
