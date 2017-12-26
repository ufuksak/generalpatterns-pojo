package com.aurea.bigcode.source

class CodeStyle {
    private static final INDENT = '    '

    /* Going with System.lineSeparator() doesn't match separators coming from groovy """ """ blocks */
    private static String LINE_SEPARATOR = '\n'

    static String indent(int n = 1) {
        INDENT * n
    }

    static String lineSeparator(int n = 0) {
        LINE_SEPARATOR + indent(n)
    }

    static String multilineSeparator(int n = 2) {
        LINE_SEPARATOR * n
    }
}
