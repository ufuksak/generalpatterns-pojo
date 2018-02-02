package com.aurea.testgenerator.value.random

import com.github.javafaker.Faker
import org.apache.commons.lang3.RandomUtils

final class RandomStringPool {

    private static final Faker FAKER = new Faker()
    private static final List<Closure<String>> PROVIDERS = Collections.unmodifiableList(Arrays.asList(
            { FAKER.space().planet() },
            { FAKER.space().moon() },
            { FAKER.space().galaxy() },
            { FAKER.space().nebula() },
            { FAKER.space().starCluster() },
            { FAKER.space().constellation() },
            { FAKER.space().star() },
            { FAKER.space().agency() },
            { FAKER.space().agencyAbbreviation() },
            { FAKER.space().nasaSpaceCraft() },
            { FAKER.space().company() },
            { FAKER.space().distanceMeasurement() }))

    static String next() {
        PROVIDERS[RandomUtils.nextInt(0, PROVIDERS.size())].call()
    }
}
