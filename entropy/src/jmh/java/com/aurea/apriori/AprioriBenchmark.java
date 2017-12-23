package com.aurea.apriori;

import com.aurea.methobase.meta.MethodMetaInformation;
import com.aurea.methobase.yaml.YamlMetaInformationRepository;
import de.mrapp.apriori.Apriori;
import de.mrapp.apriori.Transaction;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import one.util.streamex.StreamEx;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


@State(Scope.Benchmark)
public class AprioriBenchmark {

    private static final String BENCHMARK_YAML_LOCATION = "perf";
    private static final double MIN_SUPPORT = 0.01;

    @Param("")
    public String yamlFile;

    private Apriori<NamedItem> apriori;
    private Iterable<Transaction<NamedItem>> transactions;

    public static void  main(String[] args) throws RunnerException, IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(BENCHMARK_YAML_LOCATION))) {
            String[] yamlFiles = paths.map(Path::toString).filter(s -> s.endsWith(".yml")).toArray(String[]::new);

            Options opt = new OptionsBuilder()
                    .include(AprioriBenchmark.class.getSimpleName())
                    .forks(1)
                    .warmupIterations(10)
                    .measurementIterations(10)
                    .param("yamlFile", yamlFiles)
                    .build();

            new Runner(opt).run();
        }
    }

    @Setup
    public void prepare() {
        apriori = new Apriori.Builder<NamedItem>(MIN_SUPPORT).create();
        StreamEx<MethodMetaInformation> metas = YamlMetaInformationRepository.createForMethods(new File(yamlFile)).all();
        transactions = com.aurea.apriori.Runner.toTransactions(metas);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void executionPerformance() {
        apriori.execute(transactions);
    }
}
