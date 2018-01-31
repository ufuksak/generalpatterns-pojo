package com.aurea.testgenerator.value.random;

import com.aurea.testgenerator.value.TestValue;
import com.github.javaparser.ast.type.PrimitiveType;
import org.apache.commons.lang3.RandomUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class RandomPrimitive implements TestValue {

    private final static Map<PrimitiveType.Primitive, Function<Integer, String>> converters = new EnumMap<>(PrimitiveType.Primitive.class);

    static {
        converters.put(PrimitiveType.Primitive.BOOLEAN, (i) -> "true");
        converters.put(PrimitiveType.Primitive.CHAR, (i) -> Character.toString((char) i.intValue()));
        converters.put(PrimitiveType.Primitive.BYTE, String::valueOf);
        converters.put(PrimitiveType.Primitive.INT, String::valueOf);
        converters.put(PrimitiveType.Primitive.SHORT, String::valueOf);
        converters.put(PrimitiveType.Primitive.LONG, (i) -> String.valueOf(i) + "L");
        converters.put(PrimitiveType.Primitive.FLOAT, (i) -> String.valueOf(i) + ".0F");
        converters.put(PrimitiveType.Primitive.DOUBLE, (i) -> String.valueOf(i) + ".0D");
    }

    private final PrimitiveType.Primitive primitive;

    private RandomPrimitive(PrimitiveType.Primitive primitive) {
        this.primitive = primitive;
    }

    public static RandomPrimitive of(PrimitiveType.Primitive primitive) {
        return new RandomPrimitive(primitive);
    }

    @Override
    public String get() {
        int i = RandomUtils.nextInt(0, 42);
        return converters.get(primitive).apply(i);
    }
}
