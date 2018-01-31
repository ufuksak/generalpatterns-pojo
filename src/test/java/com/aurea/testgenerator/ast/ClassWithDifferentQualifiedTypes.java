package com.aurea.testgenerator.ast;

public class ClassWithDifferentQualifiedTypes {

    public String javaLang() {
        return null;
    }

    public ProcessBuilder.Redirect.Type javaLang2() {
        return null;
    }

    public ClassWithDifferentMethodDeclarations alreadyQualifiedNames() {
        return null;
    }

    public Foo innerType() {
        return null;
    }

    public Bar.Foo innerTypeNLayers() {
        return null;
    }

    public ClassWithDifferentQualifiedTypes.Foo innerTypeWithSuper() {
        return null;
    }

    private static class Bar {
        private static class Foo {
        }
    }

    private static class Foo {
    }
}
