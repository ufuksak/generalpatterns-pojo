package com.aurea.testgenerator.ast;

import java.math.BigDecimal;
import java.util.List;

public class ClassWithDifferentMethodDeclarations {

    public void allVoid() {
    }

    public boolean primitiveTypesArgs(boolean n, char c, byte b, short s, int i, long l, float f, double d) {
        return false;
    }

    public boolean[] arrays(byte[] b, char[] c, short[] s, int[] i, long[] l, float[] f, double[] d) {
        return null;
    }

    public boolean[][][][] multiarrays() {
        return null;
    }

    public void varargs(int... ints) {
    }

    public void multivarargs(int[][][][]... chars) {
    }

    public void javaLangType(Integer i) {
    }

    public void fullType(ClassWithDifferentQualifiedTypes t) {
    }

    public void innerType(Foo foo) {
    }

    public void listType(List<String> foo) {
    }

    private static class Foo {
    }

    public enum innerEnum {
        UNO;

        public String getCode() {
            return null;
        }
    }

    public interface Inter {
        void callMe(BigDecimal numeroUno);
    }
}
