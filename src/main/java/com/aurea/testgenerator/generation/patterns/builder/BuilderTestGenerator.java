package com.aurea.testgenerator.generation.patterns.builder;

import com.aurea.testgenerator.ast.ASTNodeUtils;
import com.aurea.testgenerator.ast.Callability;
import com.aurea.testgenerator.generation.TestGenerator;
import com.aurea.testgenerator.generation.TestGeneratorResult;
import com.aurea.testgenerator.reporting.CoverageReporter;
import com.aurea.testgenerator.reporting.TestGeneratorResultReporter;
import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("builder")
public class BuilderTestGenerator implements TestGenerator {

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

            List<MethodDeclaration> testableMethods = BuilderTestAnalyzer.filterTestable(builderClass.getMethods());
            for (MethodDeclaration builderMethod : testableMethods) {
                BuilderTestAnalyzer.getCorrespondingGetter(builderMethod, pojoMethods).ifPresent(getter -> {
                    TestGeneratorResult test = TestResultBuilder.buildTest(fullBuilderTypeName, fullPojoTypeName,
                            builderMethod, getter);
                    reporter.publish(test, unit, builderMethod);
                    coverageReporter.report(unit, test, builderMethod);
                    tests.add(test);
                });
            }
        });
        return tests;
    }
}
