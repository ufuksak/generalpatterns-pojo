package com.aurea.testgenerator.generation.patterns.builder;

import static com.aurea.testgenerator.generation.patterns.builder.BuilderTestTypes.BUILDER_TESTER;
import com.aurea.testgenerator.generation.TestGeneratorError;
import com.aurea.testgenerator.generation.TestGeneratorResult;
import com.aurea.testgenerator.generation.ast.DependableNode;
import com.aurea.testgenerator.generation.ast.TestDependency;
import com.aurea.testgenerator.value.Types;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.MethodUsage;

class TestResultBuilder {

    private static final String TEST_TEMPLATE = "\n"
            + "@Test\n"
            + "public void testBuild%s() {\n"
            + "    //Arrange\n"
            + "    %s testData = %s;\n"
            + "    %s builder = new %s(%s);\n"
            + "    builder.%s(testData);\n"
            + "    //Act\n"
            + "    %s pojo = builder.build();\n"
            + "    //Assert\n"
            + "    %s;\n"
            + "}\n";

    private static final String ASSERT_EQUALS = "import static org.junit.Assert.assertEquals;";
    private static final String JUNIT_TEST = "import org.junit.Test;";
    private static final String ASSERT_TEMPLATE_DEFAULT = "assertEquals(testData, pojo.%s())";
    private static final String ASSERT_TEMPLATE_FLOAT = "assertEquals(testData, pojo.%s(), 0)";
    private static final String NEW_CLASS_TEMPLATE = "new %s()";
    private static final String STRING_DATA = "\"testData\"";
    private static final String CHAR_DATA = "'c'";
    private static final String BOOLEAN_DATA = "true";
    private static final String NUMBER_DATA = "10";
    private static final String LONG_SUFFIX = "L";
    private static final String FLOAT_SUFFIX = "f";
    private static final String DOUBLE_SUFFIX = ".0";

    static TestGeneratorResult buildTest(String fullBuilderTypeName, String fullPojoTypeName,
                                         MethodDeclaration method, MethodUsage getter, String parameters) {
        TestGeneratorResult result = new TestGeneratorResult();
        result.setType(BUILDER_TESTER);

        String testText = getTestText(fullBuilderTypeName, fullPojoTypeName,
                method, getter, parameters);

        try {
            MethodDeclaration testCode = JavaParser.parseBodyDeclaration(testText).asMethodDeclaration();
            TestDependency testDependency = new TestDependency();
            testDependency.getImports().add(JavaParser.parseImport(ASSERT_EQUALS));
            testDependency.getImports().add(JavaParser.parseImport(JUNIT_TEST));
            result.getTests().add(DependableNode.from(testCode, testDependency));
        } catch (ParseProblemException ppe) {
            result.getErrors().add(TestGeneratorError.parseFailure(testText));
        }

        return result;
    }

    private static String getTestText(String fullBuilderTypeName, String fullPojoTypeName, MethodDeclaration builderMethod,
                                      MethodUsage getter, String parameters) {
        String dataType = builderMethod.getParameter(0).getType().asString();
        String testData = getTestData(builderMethod.getParameter(0).getType());
        String assertLine = getAssert(builderMethod.getParameter(0).getType(), getter.getName());

        return String.format(TEST_TEMPLATE,
                BuilderTestHelper.firstToUpperCase(builderMethod.getNameAsString()),
                dataType, testData,
                fullBuilderTypeName, fullBuilderTypeName, parameters,
                builderMethod.getNameAsString(),
                fullPojoTypeName,
                assertLine);
    }

    private static String getAssert(Type type, String name) {
        if (BuilderTestHelper.isPrimitiveOf(type, PrimitiveType.Primitive.FLOAT)
                || BuilderTestHelper.isPrimitiveOf(type, PrimitiveType.Primitive.DOUBLE)) {
            return String.format(ASSERT_TEMPLATE_FLOAT, name);
        }
        return String.format(ASSERT_TEMPLATE_DEFAULT, name);
    }

    private static String getTestData(Type paramType) {
        if (BuilderTestHelper.isPrimitive(paramType)) {
            return getPrimitiveTestData(paramType);
        }

        return String.format(NEW_CLASS_TEMPLATE, paramType.asString());
    }

    private static String getPrimitiveTestData(Type paramType) {
        if (Types.isString(paramType)) {
            return STRING_DATA;
        }

        if (BuilderTestHelper.isPrimitiveOf(paramType, PrimitiveType.Primitive.CHAR)) {
            return CHAR_DATA;
        }

        if (BuilderTestHelper.isPrimitiveOf(paramType, PrimitiveType.Primitive.BOOLEAN)) {
            return BOOLEAN_DATA;
        }

        return NUMBER_DATA + getTypeSuffix(paramType);
    }

    private static String getTypeSuffix(Type paramType) {
        if (BuilderTestHelper.isPrimitiveOf(paramType, PrimitiveType.Primitive.LONG)) {
            return LONG_SUFFIX;
        }

        if (BuilderTestHelper.isPrimitiveOf(paramType, PrimitiveType.Primitive.FLOAT)) {
            return FLOAT_SUFFIX;
        }

        if (BuilderTestHelper.isPrimitiveOf(paramType, PrimitiveType.Primitive.DOUBLE)) {
            return DOUBLE_SUFFIX;
        }

        return "";
    }
}
