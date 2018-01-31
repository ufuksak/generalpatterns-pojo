package com.aurea.testgenerator.value.random;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class RandomDateWithinRange extends RandomDate {

    private final long fromInSeconds;
    private final long toInSeconds;
    private final LocalDate from;
    private final LocalDate to;

    public RandomDateWithinRange(LocalDate from, LocalDate to) {
        fromInSeconds = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        toInSeconds = to.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        this.from = from;
        this.to = to;
    }

    public static RandomDateWithinRange between(LocalDate from, LocalDate to) {
        return new RandomDateWithinRange(from, to);
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return ImmutableList.<String>builder()
                .addAll(super.getImports())
                .add("com.redknee.framework.xgen.support.DateUtil")
                .build();
    }

    @Override
    public String get() {
        int year = RandomUtils.nextInt(from.getYear(), to.getYear());
        int month = RandomUtils.nextInt(from.getMonthValue(), to.getMonthValue());
        int day = RandomUtils.nextInt(from.getDayOfMonth(), to.getDayOfMonth());
        return String.format("newDate(\"%d-%02d-%02d\")", year, month, day);
    }
}
