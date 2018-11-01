package com.aurea.testgenerator.generation.patterns.builder;

import static com.aurea.testgenerator.generation.patterns.builder.BuilderTestTypes.BUILDER_TESTER;

import com.aurea.testgenerator.ast.ASTNodeUtils;
import com.aurea.testgenerator.ast.Callability;
import com.aurea.testgenerator.generation.TestGenerator;
import com.aurea.testgenerator.generation.TestGeneratorError;
import com.aurea.testgenerator.generation.TestGeneratorResult;
import com.aurea.testgenerator.generation.ast.DependableNode;
import com.aurea.testgenerator.generation.ast.TestDependency;
import com.aurea.testgenerator.reporting.CoverageReporter;
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter;
import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.value.Types;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import one.util.streamex.StreamEx;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("builder")
public class BuilderTestGenerator implements TestGenerator {

    private static final String TEST_TEMPLATE = "\n"
            + "@Test\n"
            + "public void testBuild%s() {\n"
            + "    //Arrange\n"
            + "    %s testData = %s;\n"
            + "    %s builder = new %s();\n"
            + "    builder.%s(testData);\n"
            + "    //Act\n"
            + "    %s pojo = builder.build();\n"
            + "    //Assert\n"
            + "    assertEquals(testData, pojo.%s());\n"
            + "}\n";
    private static final String ASSERT_EQUALS = "import static org.junit.Assert.assertEquals;";
    private static final String JUNIT_TEST = "import org.junit.Test;";
    private static final String STRING_DATA = "\"testData\"";
    private static final String CHAR_DATA = "'c'";
    private static final String BOOLEAN_DATA = "true";
    private static final String NUMBER_DATA = "10";
    private static final String LONG_SUFFIX = "L";
    private static final String FLOAT_SUFFIX = "f";
    private static final String DOUBLE_SUFFIX = ".0";
    private static final String NEW_CLASS_TEMPLATE = "new %s()";

    private final TestGeneratorResultReporter reporter;
    private final CoverageReporter coverageReporter;

    public BuilderTestGenerator(TestGeneratorResultReporter reporter, CoverageReporter coverageReporter) {
        this.reporter = reporter;
        this.coverageReporter = coverageReporter;
    }

    @Override
    public Collection<TestGeneratorResult> generate(Unit unit) {
        List<ClassOrInterfaceDeclaration> classes = unit.getCu().findAll(ClassOrInterfaceDeclaration.class)
                .stream().filter(node -> !node.isInterface()).collect(Collectors.toList());

        List<TestGeneratorResult> tests = new ArrayList<>();
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            if (Callability.isInstantiable(classDeclaration) && BuilderTestHelper
                    .isBuilder(classDeclaration)) {
                tests.addAll(buildTests(unit, classDeclaration));
            }
        }

        return tests;
    }

    private List<TestGeneratorResult> buildTests(Unit unit, ClassOrInterfaceDeclaration builderClass) {
        List<TestGeneratorResult> tests = new ArrayList<>();
        BuilderTestHelper.findBuilderMethod(builderClass).ifPresent(builder -> {
            ResolvedType resolvedType = builder.getType().resolve();

            Set<MethodUsage> pojoMethods = resolvedType.asReferenceType()
                    .getTypeDeclaration().getAllMethods();

            String fullPojoTypeName = resolvedType.asReferenceType().getTypeDeclaration().getClassName();
            String fullBuilderTypeName = ASTNodeUtils.getFullTypeName(builderClass);
            for (MethodDeclaration builderMethod : builderClass.getMethods()) {
                if (!hasTestableType(builderMethod)) {
                    continue;
                }
                getCorrespondingGetter(builderMethod, pojoMethods).ifPresent(getter -> {
                    TestGeneratorResult test = buildTest(fullBuilderTypeName, fullPojoTypeName, builderMethod, getter);
                    reporter.publish(test, unit, builderMethod);
                    coverageReporter.report(unit, test, builderMethod);
                    tests.add(test);
                });
            }
        });
        return tests;
    }

    private TestGeneratorResult buildTest(String fullBuilderTypeName, String fullPojoTypeName,
            MethodDeclaration method, MethodUsage getter) {
        TestGeneratorResult result = new TestGeneratorResult();
        result.setType(BUILDER_TESTER);

        String testText = getTestText(fullBuilderTypeName, fullPojoTypeName, method, getter);

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

    private String getTestText(String fullBuilderTypeName, String fullPojoTypeName, MethodDeclaration builderMethod,
            MethodUsage getter) {
        String dataType = builderMethod.getParameter(0).getType().asString();
        String testData = getTestData(builderMethod.getParameter(0).getType());

        return String.format(TEST_TEMPLATE,
                BuilderTestHelper.firstToUpperCase(builderMethod.getNameAsString()),
                dataType, testData,
                fullBuilderTypeName, fullBuilderTypeName,
                builderMethod.getNameAsString(),
                fullPojoTypeName,
                getter.getName());
    }

    private boolean hasTestableType(MethodDeclaration method) {
        if (method.getNameAsString().equals(BuilderTestHelper.BUILD_METHOD)) {
            return false;
        }

        if (method.getParameters().isEmpty() || method.getParameters().size() > 1) {
            return false;
        }

        Type paramType = method.getParameter(0).getType();
        if (isPrimitive(paramType)) {
            return true;
        }

        ResolvedType resolvedType = paramType.resolve();
        return resolvedType.isReferenceType()
                && resolvedType.asReferenceType().getTypeDeclaration().isClass()
                && !resolvedType.asReferenceType().getTypeDeclaration().isGeneric();
    }

    private boolean isPrimitive(Type paramType) {
        return Types.isString(paramType) || paramType.isPrimitiveType()
                || Types.isBoxedPrimitive(paramType);
    }

    private String getTestData(Type paramType) {
        if (isPrimitive(paramType)) {
            return getPrimitiveTestData(paramType);
        }

        return String.format(NEW_CLASS_TEMPLATE, paramType.asString());
    }

    private String getPrimitiveTestData(Type paramType) {
        if (Types.isString(paramType)) {
            return STRING_DATA;
        }

        if (isPrimitiveOf(paramType, Primitive.CHAR)) {
            return CHAR_DATA;
        }

        if (isPrimitiveOf(paramType, Primitive.BOOLEAN)) {
            return BOOLEAN_DATA;
        }

        return NUMBER_DATA + getTypeSuffix(paramType);
    }

    private String getTypeSuffix(Type paramType) {
        if (isPrimitiveOf(paramType, Primitive.LONG)) {
            return LONG_SUFFIX;
        }

        if (isPrimitiveOf(paramType, Primitive.FLOAT)) {
            return FLOAT_SUFFIX;
        }

        if (isPrimitiveOf(paramType, Primitive.DOUBLE)) {
            return DOUBLE_SUFFIX;
        }

        return "";
    }

    private boolean isPrimitiveOf(Type paramType, Primitive primitive) {
        return primitive.toBoxedType().asString().equals(paramType.asString())
                || primitive.asString().equals(paramType.asString());
    }

    private Optional<MethodUsage> getCorrespondingGetter(MethodDeclaration builderMethod,
            Set<MethodUsage> pojoMethods) {
        String getter = BuilderTestHelper.buildGetterName(BuilderTestHelper.GET_PREFIX, builderMethod);
        String isGetter = BuilderTestHelper.buildGetterName(BuilderTestHelper.IS_PREFIX, builderMethod);
        return StreamEx.of(pojoMethods)
                .findFirst(pojoMethod -> pojoMethod.getName().equals(getter)
                        || (pojoMethod.getName().equals(isGetter) && hasBooleanReturnType(pojoMethod)));
    }

    private boolean hasBooleanReturnType(MethodUsage pojoMethod) {
        if (!pojoMethod.returnType().isPrimitive()) {
            return false;
        }

        ResolvedPrimitiveType type = pojoMethod.returnType().asPrimitive();
        return type.describe().equals(Primitive.BOOLEAN.asString());
    }
}
