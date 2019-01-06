package com.aurea.testgenerator.generation.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.aurea.testgenerator.ast.ASTNodeUtils;
import com.aurea.testgenerator.generation.TestGeneratorResult;
import com.aurea.testgenerator.generation.ast.DependableNode;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestResultBuilderTest extends TestBase {

    @Test
    public void givenAStringTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("firstName", "getFirstName");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildFirstName() {\n"
                + "    // Arrange\n"
                + "    String testData = \"testData\";\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.firstName(testData);\n"
                + "    // Act\n"
                + "    Person2Builder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getFirstName());\n"
                + "}");
    }

    @Test
    public void givenALongTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("ssn", "getSsn");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildSsn() {\n"
                + "    // Arrange\n"
                + "    Long testData = 10L;\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.ssn(testData);\n"
                + "    // Act\n"
                + "    Person2Builder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getSsn());\n"
                + "}");
    }

    @Test
    public void givenABooleanTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("alive", "isAlive");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildAlive() {\n"
                + "    // Arrange\n"
                + "    Boolean testData = true;\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.alive(testData);\n"
                + "    // Act\n"
                + "    Person2Builder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.isAlive());\n"
                + "}");
    }

    @Test
    public void givenACharacterTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("gender", "getGender");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildGender() {\n"
                + "    // Arrange\n"
                + "    Character testData = 'c';\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.gender(testData);\n"
                + "    // Act\n"
                + "    Person2Builder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getGender());\n"
                + "}");
    }

    @Test
    public void givenAClassTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("relative", "getRelative");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildRelative() {\n"
                + "    // Arrange\n"
                + "    Person2 testData = new Person2();\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.relative(testData);\n"
                + "    // Act\n"
                + "    Person2Builder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getRelative());\n"
                + "}");
    }

    @Test
    public void givenAFloatTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("index", "getIndex");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildIndex() {\n"
                + "    // Arrange\n"
                + "    float testData = 10f;\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.index(testData);\n"
                + "    // Act\n"
                + "    PersonBuilder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getIndex(), 0);\n"
                + "}");
    }

    @Test
    public void givenADoubleTypeMethodWhenBuildTestIsCalledThenShouldProduceATest() {
        // Act
        TestGeneratorResult testResult = getTestGeneratorResult("incoming", "getIncoming");

        // Assert
        assertTestResult(testResult, "@Test\n"
                + "public void testBuildIncoming() {\n"
                + "    // Arrange\n"
                + "    double testData = 10.0;\n"
                + "    Person2Builder builder = new Person2Builder();\n"
                + "    builder.incoming(testData);\n"
                + "    // Act\n"
                + "    PersonBuilder pojo = builder.build();\n"
                + "    // Assert\n"
                + "    assertEquals(testData, pojo.getIncoming(), 0);\n"
                + "}");
    }

    private void assertTestResult(TestGeneratorResult testResult, String expectedTest) {
        assertThat(testResult.getTests().isEmpty()).isFalse();
        DependableNode<MethodDeclaration> test = testResult.getTests().get(0);
        assertThat(test.toString()).isEqualTo(expectedTest);
    }

    private TestGeneratorResult getTestGeneratorResult(String builderMethodName, String pojoMenthodName) {
        // Arrange
        ClassOrInterfaceDeclaration builderClass = getBuilderClass();
        MethodDeclaration builderMethod = getBuilderMethod(builderClass, builderMethodName);
        ResolvedType resolvedType = builderMethod.getType().resolve();
        String fullPojoTypeName = resolvedType.asReferenceType().getTypeDeclaration().getClassName();
        String fullBuilderTypeName = ASTNodeUtils.getFullTypeName(builderClass);

        MethodUsage getter = getPojoMethods(builderClass).stream()
                .filter(method -> method.getName().equals(pojoMenthodName)).collect(Collectors.toList()).get(0);

        // Act
        return TestResultBuilder.buildTest(fullBuilderTypeName, fullPojoTypeName,
                builderMethod, getter, "");
    }
}
