package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.TestUnitSpec
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import spock.lang.Unroll

class PojoMethodsFinderSpec extends TestUnitSpec {
    private static final String CLASS_WITH_INT_FIELD = """
            class Foo {
                private int field;
            
                public void setField(int field) {
                    this.field = field;
                }
                
                public int getField() {
                    return field;
                }
            }
        """

    private static final String CLASS_WITH_BOOLEAN_FIELD = """
            class Foo {
                private boolean field;
            
                public void setField(boolean field) {
                    this.field = field;
                }
                
                public boolean getField() {
                    return field;
                }
            }
        """

    private static final String CLASS_WITH_BOOLEAN_FIELD_IS = """
            class Foo {
                private boolean isField;
            
                public void setField(boolean field) {
                    isField = field;
                }
                
                public boolean isField() {
                    return field;
                }
            }
        """

    private static final String CLASS_WITH_BOOLEAN_FIELD_IS_IS = """
            class Foo {
                private boolean isField;
            
                public void setField(boolean field) {
                    isField = field;
                }
                
                public boolean isField() {
                    return isField;
                }
            }
        """

    @Unroll
    def "test pojo getter finder"() {
        setup:

        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)

        ResolvedFieldDeclaration fieldDeclaration = cu.findAll(FieldDeclaration)
                .find { field in it.variables.nameAsString }
                .resolve()

        def getterMethod = PojoMethodsFinder.findGetterMethod(fieldDeclaration)

        expect:

        getterMethod.present
        getterMethod.get().qualifiedName == getterName

        PojoFieldFinder.findGetterField(getterMethod.get()).orElse(null)?.name == field

        where:
        code                           | field     | getterName
        CLASS_WITH_INT_FIELD           | 'field'   | 'Foo.getField'
        CLASS_WITH_BOOLEAN_FIELD       | 'field'   | 'Foo.getField'
        CLASS_WITH_BOOLEAN_FIELD_IS    | 'isField' | 'Foo.isField'
        CLASS_WITH_BOOLEAN_FIELD_IS_IS | 'isField' | 'Foo.isField'
    }

    @Unroll
    def "test pojo setters finder"() {
        setup:

        CompilationUnit cu = JavaParser.parse(code)
        injectSolver(cu)

        ResolvedFieldDeclaration fieldDeclaration = cu.findAll(FieldDeclaration)
                .find { field in it.variables.nameAsString }
                .resolve()

        def setterMethod = PojoMethodsFinder.findSetterMethod(fieldDeclaration)

        expect:

        setterMethod.present
        setterMethod.get().qualifiedName == setterName

        PojoFieldFinder.findSetterField(setterMethod.get()).orElse(null)?.name == field

        where:
        code                           | field     | setterName
        CLASS_WITH_INT_FIELD           | 'field'   | 'Foo.setField'
        CLASS_WITH_BOOLEAN_FIELD       | 'field'   | 'Foo.setField'
        CLASS_WITH_BOOLEAN_FIELD_IS    | 'isField' | 'Foo.setField'
        CLASS_WITH_BOOLEAN_FIELD_IS_IS | 'isField' | 'Foo.setField'
    }
}
