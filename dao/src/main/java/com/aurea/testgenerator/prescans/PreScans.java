package com.aurea.testgenerator.prescans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PreScans {

    public static final PreScans EMPTY = with();

    private final List<PreScan> preScans;

    public PreScans(List<PreScan> preScans) {
        this.preScans = preScans;
    }

    public static PreScans with(PreScan... preScans) {
        return new PreScans(Arrays.asList(preScans));
    }

    public List<PreScan> getPreScans() {
        return preScans;
    }

    public static PreScans with(Collection<PreScan> values) {
        return new PreScans(new ArrayList<>(values));
    }
}
