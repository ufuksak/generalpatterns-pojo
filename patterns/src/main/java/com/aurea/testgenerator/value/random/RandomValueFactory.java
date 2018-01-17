package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.EmptyCollectionTestValue;
import com.aurea.testgenerator.value.TestValue;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RandomValueFactory {

    private static final Map<String, PrimitiveType.Primitive> PRIMITIVES;
    private static final Map<String, String> BOXED_TO_UNBOXED;

    static {
        Map<String, PrimitiveType.Primitive> aMap = new HashMap<>();
        Map<String, String> boxedToUnboxed = new HashMap<>();
        for (PrimitiveType.Primitive primitive : PrimitiveType.Primitive.values()) {
            aMap.put(primitive.name(), primitive);
            boxedToUnboxed.put(primitive.toBoxedType().getNameAsString(), primitive.name());
        }
        PRIMITIVES = Collections.unmodifiableMap(aMap);
        boxedToUnboxed.put("Integer", PrimitiveType.Primitive.INT.name());
        boxedToUnboxed.put("java.lang.Integer", PrimitiveType.Primitive.INT.name());
        BOXED_TO_UNBOXED = Collections.unmodifiableMap(boxedToUnboxed);
    }

    public RandomValueFactory() {

    }

    public TestValue get(String type) {
        PrimitiveType.Primitive primitive = PRIMITIVES.get(type.replace("java.lang.", "").toUpperCase());
        PrimitiveType.Primitive boxedPrimitive = PRIMITIVES.get(BOXED_TO_UNBOXED.getOrDefault(type, "").toUpperCase());
        if (null != primitive) {
            return RandomPrimitive.of(primitive);
        } else if (null != boxedPrimitive) {
            return RandomPrimitive.of(boxedPrimitive);
        } else if ("String".equals(type)) {
            return new RandomString();
        } else if ("Timestamp".equals(type)) {
            return new RandomTimestamp();
        } else if (type.contains("[]")) {
            String typeOfArray = type.replace("[]", "");
            TestValue testValue = get(typeOfArray);
            return RandomArray.arrayOf(typeOfArray, testValue);
        } else if (type.equals("List") || type.equals("Collection")) {
            //No idea what is the type
            return EmptyCollectionTestValue.LIST;
        } else if (type.equals("Set")) {
            return EmptyCollectionTestValue.SET;
        } else if (type.equals("Map")) {
            return EmptyCollectionTestValue.MAP;
        } else if (type.contains("List<") || type.contains("Collection<")) {
            int diamondStart = type.indexOf("<");
            int diamondEnd = type.indexOf(">");
            String typeOfList = type.substring(diamondStart + 1, diamondEnd);
            TestValue testValue = get(typeOfList);
            return RandomList.listOf(testValue);
        } else {
            return RandomPojo.of(type);
        }
    }
}
