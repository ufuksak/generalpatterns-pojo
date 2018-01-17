package com.aurea.testgenerator.pattern;

public class ClassNamePatternType implements PatternType {

    private final String name;

    private ClassNamePatternType(String name) {
        this.name = name;
    }

    public static ClassNamePatternType of(PatternMatcher patternMatcher) {
        return new ClassNamePatternType(patternMatcher.getClass().getSimpleName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PatternType[" + name + ']';
    }
}
