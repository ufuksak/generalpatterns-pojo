package com.aurea.testgenerator.pattern;

import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.UnitHelper;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Splitter;
import org.junit.Test;

import java.io.FileWriter;
import java.io.StringWriter;

public class SandboxTest {

    @Test
    public void visitAddsOneMatchWhenGivenAUnitWithBusinessValidationMethod() throws Exception {
        Unit unit = UnitHelper.getUnitForTestJavaFile(BusinessValidationExample.class);
        CompilationUnit cu = unit.getCu();

        ImportDeclaration declaration = new ImportDeclaration(new Name("java.util.Set"), false, false);
        cu.getImports().add(declaration);
        LastPublicMethodFinder finder = new LastPublicMethodFinder();
        cu.accept(finder, null);

        String wholeFileStr = cu.toString();
        Iterable<String> result = Splitter.on(System.lineSeparator()).split(wholeFileStr);
        FileWriter fw = new FileWriter("temp.txt");

        int i = 0;

        StringWriter stringWriter = new StringWriter();
        stringWriter.append("Right after public method!");

        for (String line : result) {
            fw.append(line).append(System.lineSeparator());
            i++;
            if (i == finder.endOfLastPublicMethod.line + 2) {
                fw.append(stringWriter.toString());
            }
        }
        fw.flush();
        fw.close();
    }

    private static class LastPublicMethodFinder extends VoidVisitorAdapter<Object> {

        private Position endOfLastPublicMethod;

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (n.isPublic()) {
                boolean hasAnnotation = n.getAnnotations().stream().anyMatch(ane -> ane.getNameAsString().equals("Override"));
                if (hasAnnotation) {
                    if (null == endOfLastPublicMethod) {
                        endOfLastPublicMethod = n.getEnd().get();
                    } else if (endOfLastPublicMethod.compareTo(n.getEnd().get()) < 0) {
                        endOfLastPublicMethod = n.getEnd().get();
                    }
                }
            }
            super.visit(n, arg);
        }
    }
}
