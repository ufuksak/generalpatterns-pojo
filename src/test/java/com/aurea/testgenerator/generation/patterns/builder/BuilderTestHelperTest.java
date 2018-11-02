package com.aurea.testgenerator.generation.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BuilderTestHelperTest extends TestBase {

    @Test
    public void givenAClassWithBuilderSuffixAndBuilderMethodWhenIsBuilderIsCalledThenItShouldReturnTrue() {
        // Arrange
        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface() && node.getNameAsString().equals("PersonBuilder"))
                .collect(Collectors.toList());

        // Act/Assert
        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isTrue();
    }

    @Test
    public void givenAClassWithoutBuilderSuffixWhenIsBuilderIsCalledThenItShouldReturnFalse() {
        // Arrange
        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface()
                        && node.getNameAsString().equals("SomeBuilderWithNoBuilderSuffix"))
                .collect(Collectors.toList());

        // Act/Assert
        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isFalse();
    }

    @Test
    public void givenAClassWithoutBuildMethodWhenIsBuilderIsCalledThenItShouldReturnFalse() {
        // Arrange
        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface()
                        && node.getNameAsString().equals("SomeClassWithNoBuildMethodBuilder"))
                .collect(Collectors.toList());

        // Act/Assert
        assertThat(BuilderTestHelper.isBuilder(classes.get(0))).isFalse();
    }

    @Test
    public void givenAnEmptyTextWhenFirstToUpperCaseIsCalledThenItShouldReturnTheSameValue() {
        // Arrange
        String text = "";

        // Act/Assert
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo(text);
    }

    @Test
    public void givenAnATextWithLen1WhenFirstToUpperCaseIsCalledThenItShouldReturnItUppercased() {
        // Arrange
        String text = "c";

        // Act/Assert
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo("C");
    }

    @Test
    public void givenAnATextWhenFirstToUpperCaseIsCalledThenItShouldReturnItCapitalized() {
        // Arrange
        String text = "upperCase";

        // Act/Assert
        assertThat(BuilderTestHelper.firstToUpperCase(text)).isEqualTo("UpperCase");
    }

    @Test
    public void givenAMethodWhenBuildGetterNameIsCalledThenItShouldReturnGetterName() {
        // Arrange
        List<MethodDeclaration> classes = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(node -> node.getNameAsString().equals("firstName"))
                .collect(Collectors.toList());

        // Act/Assert
        assertThat(BuilderTestHelper.buildGetterName("get", classes.get(0))).isEqualTo("getFirstName");
    }

    @Test
    public void givenAMethodStartingWithWithWhenBuildGetterNameIsCalledThenItShouldReturnGetterName() {
        // Arrange
        List<MethodDeclaration> classes = compilationUnit.findAll(MethodDeclaration.class)
                .stream().filter(node -> node.getNameAsString().equals("withFirstName"))
                .collect(Collectors.toList());

        // Act/Assert
        assertThat(BuilderTestHelper.buildGetterName("get", classes.get(0))).isEqualTo("getFirstName");
    }

}
