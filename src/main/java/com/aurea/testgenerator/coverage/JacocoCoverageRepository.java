package com.aurea.testgenerator.coverage;

import com.aurea.coverage.CoverageIndex;
import com.aurea.coverage.parser.JacocoParsers;
import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.MethodCoverage;
import com.aurea.coverage.unit.Named;
import com.aurea.coverage.unit.PackageCoverage;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class JacocoCoverageRepository implements CoverageRepository {

    private final CoverageIndex index;

    private final Supplier<Map<String, Set<MethodCoverage>>> coveredMethodsCache = Suppliers.memoize(new Supplier<Map<String, Set<MethodCoverage>>>() {
        @Override
        public Map<String, Set<MethodCoverage>> get() {
            return index.getModuleCoverage().methodCoverages().collect(groupingBy(Named::getName, Collectors.toSet()));
        }
    });

    private final Supplier<Map<String, Map<String, ClassCoverage>>> packageIndex = Suppliers.memoize(new Supplier<Map<String, Map<String, ClassCoverage>>>() {
        @Override
        public Map<String, Map<String, ClassCoverage>> get() {
            Collector<PackageCoverage, ?, Map<String, Map<String, ClassCoverage>>> mapPackageNamesToMapsOfCoverages =
                    toMap(Named::getName, pc -> {
                        Collector<ClassCoverage, ?, Map<String, ClassCoverage>> mapClassCoveragesToNames = toMap(Named::getName, Function.identity());
                        return pc.classCoverages().collect(mapClassCoveragesToNames);
                    });
            return index.getModuleCoverage().packageCoverages().collect(mapPackageNamesToMapsOfCoverages);
        }
    });

    private final Supplier<ImmutableMap<String, Map<String, Map<String, MethodCoverage>>>> moduleIndex =
            Suppliers.memoize(new Supplier<ImmutableMap<String, Map<String, Map<String, MethodCoverage>>>>() {
        @Override
        public ImmutableMap<String, Map<String, Map<String, MethodCoverage>>> get() {
            Stream<PackageCoverage> packageCoverages = index.getModuleCoverage().packageCoverages();
            Collector<PackageCoverage, ?, Map<String, Map<String, Map<String, MethodCoverage>>>> mapPackageCoveragesToNames =
                    toMap(Named::getName, pc -> {
                        Collector<ClassCoverage, ?, Map<String, Map<String, MethodCoverage>>> mapClassCoveragesToMethodCoverages =
                                toMap(Named::getName, cc -> {
                                    Collector<MethodCoverage, ?, Map<String, MethodCoverage>> mapMethodCoveragesToNames = toMap(Named::getName, Function.identity(),
                                            (mc1, mc2) -> {
                                                if (mc1.getTotal() > mc2.getTotal()) {
                                                    return mc1;
                                                } else {
                                                    return mc2;
                                                }
                                            });
                                    return cc.methodCoverages().distinct().collect(mapMethodCoveragesToNames);
                                });
                        return pc.classCoverages().collect(mapClassCoveragesToMethodCoverages);
                    });
            Map<String, Map<String, Map<String, MethodCoverage>>> collected = packageCoverages.collect(mapPackageCoveragesToNames);
            return ImmutableMap.copyOf(collected);
        }
    });

    public static JacocoCoverageRepository fromFile(Path pathToJacoco) {
        return new JacocoCoverageRepository(JacocoParsers.fromXml(pathToJacoco));
    }

    public JacocoCoverageRepository(CoverageIndex index) {
        this.index = index;
    }

    @Override
    public Optional<ClassCoverage> getClassCoverage(ClassCoverageCriteria criteria) {
        if (Strings.isNullOrEmpty(criteria.getClassName())) {
            throw new IllegalArgumentException("Class name must be provided!");
        }
        if (Strings.isNullOrEmpty(criteria.getPackageName())) {
            throw new IllegalArgumentException("Package name must be provided!");
        }
        return Optional.ofNullable(packageIndex.get()
                .getOrDefault(criteria.getPackageName(), emptyMap())
                .get(criteria.getClassName()));
    }

    @Override
    public Optional<MethodCoverage> getMethodCoverage(MethodCoverageCriteria criteria) {
        if (Strings.isNullOrEmpty(criteria.getMethodName())) {
            throw new IllegalArgumentException("Method name must be provided!");
        }
        if (Strings.isNullOrEmpty(criteria.getClassName())) {
            throw new IllegalArgumentException("Class name must be provided!");
        }
        if (Strings.isNullOrEmpty(criteria.getPackageName())) {
            throw new IllegalArgumentException("Package name must be provided!");
        }
        return Optional.ofNullable(moduleIndex.get()
                .getOrDefault(criteria.getPackageName(), emptyMap())
                .getOrDefault(criteria.getClassName(), emptyMap())
                .get(criteria.getMethodName()));
    }

    @Override
    public Collection<MethodCoverage> getMethodCoverages(MethodCoverageCriteria query) {
        Objects.requireNonNull(query.getMethodName());
        return coveredMethodsCache.get().getOrDefault(query.getMethodName(), emptySet());
    }
}
