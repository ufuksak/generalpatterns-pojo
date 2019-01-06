package com.aurea.testgenerator.generation.patterns.builder;

import com.aurea.testgenerator.ast.ASTNodeUtils;
import com.aurea.testgenerator.ast.Callability;
import com.aurea.testgenerator.generation.TestGenerator;
import com.aurea.testgenerator.generation.TestGeneratorResult;
import com.aurea.testgenerator.reporting.CoverageReporter;
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter;
import com.aurea.testgenerator.source.Unit;
import com.aurea.testgenerator.value.Types;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.apache.logging.log4j.core.util.Patterns.COMMA_SEPARATOR;

@Component
@Profile("builder")
public class BuilderTestGenerator implements TestGenerator {
    private static final Logger logger = LogManager.getLogger(BuilderTestGenerator.class.getSimpleName());

    private final TestGeneratorResultReporter reporter;
    private final CoverageReporter coverageReporter;
    private static final String PUBLIC = "PUBLIC";
    private static final String PRIVATE = "private";
    private static final String NULL = "null";
    private static final String SPACE = " ";
    private static final String BOOLEAN_PRIMITIVE = "boolean";
    private static final String BOOLEAN = "Boolean";
    private static final String STRING = "String";
    private static final String INT = "int";
    private static final String INTEGER = "Integer";
    private static final String LONG_PRIMITIVE = "long";
    private static final String LONG = "Long";
    private static final String FLOAT_PRIMITIVE = "float";
    private static final String FLOAT = "Float";
    private static final String DOUBLE_PRIMITIVE = "double";
    private static final String DOUBLE = "Double";
    private static final String ZERO = "0";
    private static final String LONG_KEY = "L";
    private static final String QUOTATION_MARKS = "\"";

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
            if (Callability.isInstantiable(classDeclaration)
                    && BuilderTestHelper.isBuilder(classDeclaration)
                    && !classDeclaration.isAbstract() && !classDeclaration.isInterface()
                    && !classDeclaration.isInnerClass()) {
                tests.addAll(buildTests(unit, classDeclaration));
            }
        }

        return tests;
    }

    private List<TestGeneratorResult> buildTests(Unit unit, ClassOrInterfaceDeclaration builderClass) {
        List<TestGeneratorResult> tests = new ArrayList<>();
        BuilderTestHelper.findBuilderMethod(builderClass).ifPresent((MethodDeclaration builder) -> {
            ResolvedType resolvedType = null;
            Set<MethodUsage> pojoMethods = new HashSet<>();
            try {
                if (builder.getType().isClassOrInterfaceType()) {
                    ClassOrInterfaceType coiType = builder.getType().asClassOrInterfaceType();
                    coiType.removeTypeArguments();
                    resolvedType = coiType.resolve();
                }
            } catch (UnsolvedSymbolException e) {
                logger.trace("cannot resolve class type", e);
                resolvedType = null;
            }

            try {
                if (resolvedType != null && resolvedType.isReferenceType()) {
                    pojoMethods = resolvedType.asReferenceType().getTypeDeclaration().getAllMethods();
                }
            } catch (UnsolvedSymbolException e) {
                logger.trace("cannot resolve all method declarations", e);
            }

            if (resolvedType != null && resolvedType.isReferenceType()) {
                if (pojoMethods.isEmpty()) {
                    for (ResolvedMethodDeclaration pojoMethod : resolvedType.asReferenceType()
                            .getTypeDeclaration().getDeclaredMethods()) {
                        if (pojoMethod.accessSpecifier().name().equals(PUBLIC)
                                && !pojoMethod.isGeneric()
                                && pojoMethod.getNumberOfSpecifiedExceptions() == 0) {
                            try {
                                pojoMethods.add(new MethodUsage(pojoMethod));
                            } catch (UnsolvedSymbolException e) {
                                logger.trace("cannot resolve method specific return type", e);
                            }
                        }
                    }
                }

                if (!pojoMethods.isEmpty()) {
                    String fullPojoTypeName = resolvedType.asReferenceType().getTypeDeclaration().getClassName();
                    String fullBuilderTypeName = ASTNodeUtils.getFullTypeName(builderClass);

                    List<MethodDeclaration> testableMethods = BuilderTestAnalyzer.filterTestable(builderClass.getMethods());
                    for (MethodDeclaration builderMethod : testableMethods) {
                        String nonPrivateConstructorArgs = buildNonPrivateConstructorArgs(builderMethod);
                        if (!nonPrivateConstructorArgs.contains(PRIVATE)) {
                            BuilderTestAnalyzer.getCorrespondingGetter(builderMethod, pojoMethods).ifPresent(getter -> {
                                TestGeneratorResult test = TestResultBuilder.buildTest(fullBuilderTypeName, fullPojoTypeName,
                                        builderMethod, getter, nonPrivateConstructorArgs);
                                reporter.publish(test, unit, builderMethod);
                                coverageReporter.report(unit, test, builderMethod);
                                tests.add(test);
                            });
                        }
                    }
                }
            }
        });
        return tests;
    }

    static String buildNonPrivateConstructorArgs(final MethodDeclaration method) {
        final StringBuilder builder = new StringBuilder();
        final boolean[] nestedConstructorFound = {false};
        method.findCompilationUnit().get().getTypes().forEach(it -> {
            final List<BodyDeclaration<?>> members = it.getMembers();
            if (members != null) {
                for (final BodyDeclaration<?> member : members) {
                    if (member instanceof ClassOrInterfaceDeclaration) {
                        for (Node node : member.getChildNodes()) {
                            if (node instanceof ConstructorDeclaration) {
                                final ConstructorDeclaration constructor = (ConstructorDeclaration) node;
                                if (method.getType().toString().contains(constructor.getName().asString())) {
                                    builder.append(constructor
                                            .getParameters().stream().map(param -> buildConstructorArgument(method,
                                                    param.getType(), param.getName().toString()))
                                            .collect(Collectors.joining(COMMA_SEPARATOR)));
                                    nestedConstructorFound[0] = true;
                                    break;
                                }
                            } else if (node instanceof MethodDeclaration) {
                                final MethodDeclaration methodDeclaration = (MethodDeclaration) node;
                                if (methodDeclaration.getType().asString().contains(method.getType().toString())) {
                                    nestedConstructorFound[0] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!nestedConstructorFound[0]) {
                    for (final BodyDeclaration<?> member : members) {
                        if (member instanceof ConstructorDeclaration) {
                            final ConstructorDeclaration constructor = (ConstructorDeclaration) member;
                            if (!constructor.toString().contains("protected")) {
                                builder.append(constructor
                                        .getParameters().stream().map(param -> buildConstructorArgument(method,
                                                param.getType(), param.getName().toString()))
                                        .collect(Collectors.joining(COMMA_SEPARATOR)));
                                break;
                            }
                            if (constructor.toString().contains(PRIVATE)) {
                                builder.append(PRIVATE);
                                break;
                            }
                        }
                    }
                }
            }
        });
        return builder.toString().replace("\\s*", "");
    }

    static String buildConstructorArgument(final MethodDeclaration method, final Type type, final String name) {
        if (isClassOrInterface(type)) {
            return NULL;
        } else {
            return createRandomValue(type);
        }
    }

    static boolean isClassOrInterface(final Type type) {
        return Objects.nonNull(type) && type.isClassOrInterfaceType() && !Types.isString(type)
                && !Types.isBoxedPrimitive(type);
    }

    static String createRandomValue(final Type type) {
        final String typeString = type.toString();
        if (type.isArrayType()) {
            return "new" + SPACE + typeString + "{1}";
        }
        switch (typeString) {
            case BOOLEAN_PRIMITIVE:
            case BOOLEAN:
                return String.valueOf(RandomUtils.nextBoolean());
            case STRING:
                return QUOTATION_MARKS + RandomStringUtils.randomAlphabetic(4) + QUOTATION_MARKS;
            case INT:
            case INTEGER:
                return String.valueOf(RandomUtils.nextInt());
            case LONG_PRIMITIVE:
            case LONG:
                return RandomUtils.nextLong() + LONG_KEY;
            case FLOAT_PRIMITIVE:
            case FLOAT:
                return String.valueOf(RandomUtils.nextFloat());
            case DOUBLE_PRIMITIVE:
            case DOUBLE:
                return String.valueOf(RandomUtils.nextDouble());
            default:
                return ZERO;
        }
    }

}
