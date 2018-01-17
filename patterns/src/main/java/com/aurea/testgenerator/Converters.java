package com.aurea.testgenerator;

import com.google.common.base.Splitter;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import pl.allegro.finance.tradukisto.ValueConverters;

public final class Converters {

    public static String integerToPascalCaseWords(int number) {
        String withDashes = ValueConverters.ENGLISH_INTEGER.asWords(number);
        return StreamEx.of(Splitter.on("-").split(withDashes).iterator())
                .map(StringUtils::capitalize).joining("");
    }

    private Converters() {}
}
