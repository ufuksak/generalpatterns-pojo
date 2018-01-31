package com.aurea.testgenerator.generation

import com.aurea.testgenerator.pattern.ClassDescription
import com.aurea.testgenerator.pattern.PatternMatch

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.function.Consumer

abstract class TestGenerator<T extends PatternMatch> implements Consumer<Map<ClassDescription, List<T>>> {
    @SuppressWarnings("unchecked")
    Class<T> collects() {
        Type mySuperclass = getClass().getGenericSuperclass()
        Type tType = ((ParameterizedType)mySuperclass).getActualTypeArguments()[0]
        return (Class<T>) Class.forName(tType.getTypeName())
    }
}