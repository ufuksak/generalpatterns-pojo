package com.aurea.testgenerator.value;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;

public class CustomExpression implements TestValue {

    private final String expression;
    private final ImmutableCollection<String> imports;

    public CustomExpression(String expression, Collection<String> imports) {
        this.expression = expression;
        this.imports = ImmutableList.copyOf(imports);
    }

    public CustomExpression(String expression) {
        this(expression, Collections.emptyList());
    }

    @Override
    public ImmutableCollection<String> getImports() {
        return imports;
    }

    @Override
    public String get() {
        return expression;
    }


}
