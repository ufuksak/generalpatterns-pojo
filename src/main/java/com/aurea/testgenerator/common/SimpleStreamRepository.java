package com.aurea.testgenerator.common;

import one.util.streamex.StreamEx;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SimpleStreamRepository<T> {

    private List<T> list = new ArrayList<>();

    public void add(T value) {
        list.add(value);
    }

    public StreamEx<T> get() {
        return StreamEx.of(list);
    }
}
