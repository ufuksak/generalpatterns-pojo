package com.aurea.testgenerator.ast;

public final class ImportWrapper {

    public static String asImportStatement(String fullClassName) {
        return "import " + fullClassName + ";";
    }

    public static String asImportLine(String fullClassName) {
        return asImportStatement(fullClassName) + System.lineSeparator();
    }
}
