package com.aurea.testgenerator.generation.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class BuilderTestAnalyzerTest extends TestBase {

    @Test
    public void givenAListOfMethodsWhenFilterTestableIsCalledThenShouldReturnOnlyTheTestableMethods() {
        // Arrange
        ClassOrInterfaceDeclaration builderClass = getBuilderClass();
        // Act
        List<MethodDeclaration> testableMethods = BuilderTestAnalyzer.filterTestable(builderClass.getMethods());

        // Assert
        List<String> names = testableMethods.stream().map(method -> method.getName().asString())
                .collect(Collectors.toList());
        assertThat(names).containsExactly("firstName", "lastName", "numberOfChildren", "ssn", "alive", "gender",
                "relative", "someMethod", "index", "incoming");
    }

    @Test
    public void givenAnIsGetterWhenGetCorrespondingGetterIsCalledThenShouldReturnTheGetter() {
        // Arrange
        ClassOrInterfaceDeclaration builderClass = getBuilderClass();

        Set<MethodUsage> pojoMethods = getPojoMethods(builderClass);

        MethodDeclaration builderMethod = getBuilderMethod(builderClass, "alive");

        // Act
        Optional<MethodUsage> getter = BuilderTestAnalyzer.getCorrespondingGetter(builderMethod, pojoMethods);

        // Assert
        assertThat(getter.isPresent()).isTrue();
        assertThat(getter.get().getName()).isEqualTo("isAlive");
    }

    @Test
    public void givenAnExistingGetterWhenGetCorrespondingGetterIsCalledThenShouldReturnTheGetter() {
        // Arrange
        ClassOrInterfaceDeclaration builderClass = getBuilderClass();

        Set<MethodUsage> pojoMethods = getPojoMethods(builderClass);

        MethodDeclaration builderMethod = getBuilderMethod(builderClass, "firstName");

        // Act
        Optional<MethodUsage> getter = BuilderTestAnalyzer.getCorrespondingGetter(builderMethod, pojoMethods);

        // Assert
        assertThat(getter.isPresent()).isTrue();
        assertThat(getter.get().getName()).isEqualTo("getFirstName");
    }

    @Test
    public void givenAnNonExistingGetterWhenGetCorrespondingGetterIsCalledThenShouldReturnEmpty() {
        // Arrange
        ClassOrInterfaceDeclaration builderClass = getBuilderClass();

        Set<MethodUsage> pojoMethods = getPojoMethods(builderClass);

        MethodDeclaration builderMethod = getBuilderMethod(builderClass, "someMethod");

        // Act
        Optional<MethodUsage> getter = BuilderTestAnalyzer.getCorrespondingGetter(builderMethod, pojoMethods);

        // Assert
        assertThat(getter.isPresent()).isFalse();
    }
}