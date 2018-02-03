package com.aurea.testgenerator.coverage;

import com.aurea.coverage.CoverageIndex;
import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;
import com.aurea.coverage.unit.ModuleCoverage;
import com.aurea.coverage.unit.PackageCoverage;
import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;

import static com.github.javaparser.ast.NodeList.nodeList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class JacocoCoverageServiceTest {

    private static final MethodCoverage METHOD_COVERAGE = new MethodCoverage("bar(Dinosaur[])", 10, 10, 10, 10);
    private ClassOrInterfaceDeclaration innerDeclaration = new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "Inner");
    private ClassOrInterfaceDeclaration dinosaurDeclaration = new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "Dinosaur");
    private ClassOrInterfaceDeclaration fooDeclaration = new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "Foo");

    private JacocoCoverageService service = serviceWithCoverageInFooClass();

    @Test
    public void methodCoverageIsFoundForMethodWithArrayParameter() throws Exception {
        MethodDeclaration md = simpleMethodDeclaration();
        MethodCoverageQuery query = queryForFooClass(md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(Dinosaur[])");
    }

    @Test
    public void methodCoverageIsFoundForMethodWithVarargs() throws Exception {
        MethodDeclaration md = simpleMethodDeclaration();
        md.getParameter(0).setVarArgs(true);
        MethodCoverageQuery query = queryForFooClass(md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(Dinosaur[])");
    }

    @Test
    public void methodCoverageIsFoundForMethodWithFullySpecifiedParameter() throws Exception {
        MethodDeclaration md = new MethodDeclaration();
        md.setName("bar");
        ReferenceType classOrInterfaceType = new ArrayType(new ClassOrInterfaceType(new ClassOrInterfaceType(JavaParser.parseClassOrInterfaceType("org"), "example"), "Dinosaur"));
        Parameter parameter = new Parameter(classOrInterfaceType, "id");
        md.setParameters(nodeList(parameter));
        MethodCoverageQuery query = queryForFooClass(md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(Dinosaur[])");
    }

    @Test
    public void methodCoverageIsFoundForMethodWithGenericType() throws Exception {
        service = serviceWithCoverageInFooClass(new MethodCoverage("bar(List)", 10, 10, 10, 10));
        MethodDeclaration md = new MethodDeclaration();
        md.setName("bar");
        md.setTypeParameters(nodeList(new TypeParameter("T")));
        ClassOrInterfaceType classOrInterfaceType = JavaParser.parseClassOrInterfaceType("List");
        classOrInterfaceType.setTypeArguments(nodeList(JavaParser.parseClassOrInterfaceType("T")));
        Parameter parameter = new Parameter(classOrInterfaceType, "id");
        md.setParameters(nodeList(parameter));

        ClassOrInterfaceDeclaration cd = new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "Foo");
        cd.setTypeParameters(nodeList(new TypeParameter("T", nodeList(Collections.emptyList()))));
        MethodCoverageQuery query = queryForFooClass(md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(List)");
    }

    @Test
    public void methodCoverageIsFoundInInnerClass() throws Exception {
        service = serviceWithInnerClassInFooClass("Inner", METHOD_COVERAGE);

        MethodDeclaration md = simpleMethodDeclaration();
        fooDeclaration.setMembers(nodeList(innerDeclaration));
        CompilationUnit cu = newFooCu();
        MethodCoverageQuery query = MethodCoverageQuery.of(new Unit(cu, "org.example.Foo", Paths.get("")), innerDeclaration, md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(Dinosaur[])");
    }

    @Test
    public void methodCoverageIsFoundInInnerEnum() throws Exception {
        JacocoCoverageService service = serviceWithInnerClassInFooClass("MyEnum", METHOD_COVERAGE);

        MethodDeclaration md = simpleMethodDeclaration();
        EnumDeclaration inner = new EnumDeclaration(EnumSet.of(Modifier.PUBLIC), "MyEnum");
        md.setParentNode(inner);
        fooDeclaration.setMembers(nodeList(inner));
        CompilationUnit cu = newFooCu();
        MethodCoverageQuery query = MethodCoverageQuery.of(new Unit(cu, "org.example.Foo", Paths.get("")), md);

        MethodCoverage methodCoverage = service.getMethodCoverage(query);

        assertThat(methodCoverage.getName()).isEqualTo("bar(Dinosaur[])");
    }


    private MethodDeclaration simpleMethodDeclaration() {
        MethodDeclaration md = new MethodDeclaration();
        md.setName("bar");
        ReferenceType type = new ArrayType(JavaParser.parseClassOrInterfaceType("Dinosaur"));
        md.setParameters(nodeList(new Parameter(type, "id")));
        return md;
    }

    private MethodCoverageQuery queryForFooClass(MethodDeclaration md) {
        return queryForClass(new ClassOrInterfaceDeclaration(EnumSet.of(Modifier.PUBLIC), false, "Foo"), md);
    }

    private MethodCoverageQuery queryForClass(ClassOrInterfaceDeclaration cd, MethodDeclaration md) {
        return MethodCoverageQuery.of(new Unit(new CompilationUnit(), "org.example.Foo", Paths.get("")), cd, md);
    }

    private JacocoCoverageService serviceWithCoverageInFooClass() {
        return serviceWithCoverageInFooClass(METHOD_COVERAGE);
    }

    private JacocoCoverageService serviceWithCoverageInFooClass(MethodCoverage methodCoverage) {
        return serviceWithCoverageInClass(new ClassCoverageImpl("Foo", singletonList(methodCoverage)));
    }

    private JacocoCoverageService serviceWithCoverageInClass(ClassCoverage classCoverage) {
        ModuleCoverage moduleCoverage = new ModuleCoverage("test-module",
                singletonList(
                        new PackageCoverage("org.example",
                                singletonList(classCoverage)))
        );
        JacocoCoverageRepository repository = new JacocoCoverageRepository(new CoverageIndex(moduleCoverage));
        return new JacocoCoverageService(repository);
    }

    private JacocoCoverageService serviceWithInnerClassInFooClass(String innerClassName, MethodCoverage methodCoverage) {
        ModuleCoverage moduleCoverage = new ModuleCoverage("test-module", singletonList(
                new PackageCoverage("org.example", singletonList(
                        new ClassCoverageImpl("Foo$" + innerClassName, singletonList(
                                methodCoverage))
                ))
        ));

        JacocoCoverageRepository repository = new JacocoCoverageRepository(new CoverageIndex(moduleCoverage));
        return new JacocoCoverageService(repository);
    }

    private CompilationUnit newFooCu() {
        CompilationUnit cu = new CompilationUnit();
        cu.setTypes(nodeList(fooDeclaration));
        return cu;
    }
}