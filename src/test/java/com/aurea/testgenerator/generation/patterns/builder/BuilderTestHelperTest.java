package com.aurea.testgenerator.generation.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BuilderTestHelperTest {

    private static String JAVA_CODE = "public class Person {\n"
            + "\n"
            + "    private String firstName;\n"
            + "    private String lastName;\n"
            + "\n"
            + "    public String getFirstName() {\n"
            + "        return firstName;\n"
            + "    }\n"
            + "\n"
            + "    public void setFirstName(String firstName) {\n"
            + "        this.firstName = firstName;\n"
            + "    }\n"
            + "\n"
            + "    public String getLastName() {\n"
            + "        return lastName;\n"
            + "    }\n"
            + "\n"
            + "    public void setLastName(String lastName) {\n"
            + "        this.lastName = lastName;\n"
            + "    }\n"
            + "}"
            + ""
            + ""
            + "public class PersonBuilder {\n"
            + "\n"
            + "    private String firstName;\n"
            + "    private String lastName;\n"
            + "\n"
            + "    public PersonBuilder firstName(String firstName) {\n"
            + "        this.firstName = firstName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public PersonBuilder lastName(String lastName) {\n"
            + "        this.lastName = lastName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public Person build() {\n"
            + "        Person person = new Person();\n"
            + "        person.setFirstName(firstName);\n"
            + "        person.setLastName(lastName);\n"
            + "        return person;\n"
            + "    }\n"
            + "\n"
            + "}"
            + ""
            + "public class SomeBuilderWithNoBuilderSuffix {\n"
            + "\n"
            + "    private String firstName;\n"
            + "    private String lastName;\n"
            + "\n"
            + "    public PersonBuilder firstName(String firstName) {\n"
            + "        this.firstName = firstName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public PersonBuilder lastName(String lastName) {\n"
            + "        this.lastName = lastName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public Person build() {\n"
            + "        Person person = new Person();\n"
            + "        person.setFirstName(firstName);\n"
            + "        person.setLastName(lastName);\n"
            + "        return person;\n"
            + "    }\n"
            + "\n"
            + "}"
            + ""
            + "public class SomeClassWithNoBuildMethodBuilder {\n"
            + "\n"
            + "    private String firstName;\n"
            + "    private String lastName;\n"
            + "\n"
            + "    public PersonBuilder firstName(String firstName) {\n"
            + "        this.firstName = firstName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public PersonBuilder lastName(String lastName) {\n"
            + "        this.lastName = lastName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public Person buildThePojo() {\n"
            + "        Person person = new Person();\n"
            + "        person.setFirstName(firstName);\n"
            + "        person.setLastName(lastName);\n"
            + "        return person;\n"
            + "    }\n"
            + "\n"
            + "}"
            + ""
            + "public class SomeClassWithoutCorrespondingGettersBuilder {\n"
            + "\n"
            + "    private String firstName;\n"
            + "    private String lastName;\n"
            + "\n"
            + "    public PersonBuilder theFirstName(String firstName) {\n"
            + "        this.firstName = firstName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public PersonBuilder theLastName(String lastName) {\n"
            + "        this.lastName = lastName;\n"
            + "        return this;\n"
            + "    }\n"
            + "\n"
            + "    public Person build() {\n"
            + "        Person person = new Person();\n"
            + "        person.setFirstName(firstName);\n"
            + "        person.setLastName(lastName);\n"
            + "        return person;\n"
            + "    }\n"
            + "\n"
            + "}"
            + "";

    @Test
    public void givenAClassWithBuilderSuffixAndBuilderMethodWhenIsBuilderIsCalledThenItShouldReturnTrue() {
        CompilationUnit cu = JavaParser.parse(JAVA_CODE);

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface() && node.getNameAsString().equals("PersonBuilder"))
                .collect(Collectors.toList());

        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isTrue();
    }

    @Test
    public void givenAClassWithoutBuilderSuffixWhenIsBuilderIsCalledThenItShouldReturnFalse() {
        CompilationUnit cu = JavaParser.parse(JAVA_CODE);

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface()
                        && node.getNameAsString().equals("SomeBuilderWithNoBuilderSuffix"))
                .collect(Collectors.toList());

        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isFalse();
    }

    @Test
    public void givenAClassWithoutBuildMethodWhenIsBuilderIsCalledThenItShouldReturnFalse() {
        CompilationUnit cu = JavaParser.parse(JAVA_CODE);

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface()
                        && node.getNameAsString().equals("SomeClassWithNoBuildMethodBuilder"))
                .collect(Collectors.toList());

        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isFalse();
    }

    @Test
    public void givenAnEmptyTextWhenFirstToUpperCaseIsCalledThenItShouldReturnTheSameValue() {
        String text = "";
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo(text);
    }

    @Test
    public void givenAnATextWithLen1WhenFirstToUpperCaseIsCalledThenItShouldReturnItUppercased() {
        String text = "c";
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo("C");
    }

    @Test
    public void givenAnATextWhenFirstToUpperCaseIsCalledThenItShouldReturnItCapitalized() {
        String text = "upperCase";
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo("UpperCase");
    }

}
