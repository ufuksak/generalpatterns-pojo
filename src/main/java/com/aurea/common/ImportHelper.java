package com.aurea.common;

import com.aurea.testgenerator.generation.ast.TestUnit;
import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;

import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.ImportOrderer;
import com.google.googlejavaformat.java.RemoveUnusedImports;

public class ImportHelper {

    public static void removeWildcardImport(TestUnit testUnit, Unit unitUnderTest) {
        NodeList<ImportDeclaration> importDeclarations = testUnit.getImports();
        for (int i = 0; i < importDeclarations.size(); i++) {
            ImportDeclaration id = testUnit.getImports().get(i);
            if (id.isAsterisk() && unitUnderTest.getPackageName().startsWith(id.getNameAsString())) {
                testUnit.getImports().remove(i);
            }
        }
    }

    public static String organizeImports(String fileContents) {
        try {
            fileContents = RemoveUnusedImports.removeUnusedImports(fileContents);
            fileContents = ImportOrderer.reorderImports(fileContents);
            return fileContents;
        } catch (FormatterException e) {
            // If there's an error with the Formatter, just use original contents
            return fileContents;
        }
    }
}
