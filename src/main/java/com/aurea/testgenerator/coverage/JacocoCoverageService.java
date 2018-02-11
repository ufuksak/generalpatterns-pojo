package com.aurea.testgenerator.coverage;

import com.aurea.coverage.unit.ClassCoverage;
import com.aurea.coverage.unit.ClassCoverageImpl;
import com.aurea.coverage.unit.MethodCoverage;
import com.aurea.testgenerator.ast.ASTNodeUtils;
import com.aurea.testgenerator.source.Unit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.google.common.base.Splitter;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.aurea.testgenerator.ast.ASTNodeUtils.findAncestorSubTypeOf;

public class JacocoCoverageService implements CoverageService {

    private static final Logger logger = LogManager.getLogger(JacocoCoverageService.class
            .getSimpleName());

    private static final String COMMON_MAVEN_JAVA_MODULE_PREFIX = "src.main.java.";

    private final CoverageRepository coverageRepository;

    public JacocoCoverageService(CoverageRepository coverageRepository) {
        this.coverageRepository = coverageRepository;
    }

    @Override
    public MethodCoverage getMethodCoverage(MethodCoverageQuery query) {
        if (isAbstractOrInterfaceMethod(query)) {
            return new MethodCoverage(query.getMethod().getNameAsString(), 0, 0, 0, 0);
        }
        final String packageName = query.getUnit().getPackageName();
        final String className = toClassCoverageName(query.getUnit(), query.getType(), query
                .getAnonymousClassIndex());
        final String methodName = toJacocoMethodNameRegex(query);
        return coverageRepository.getMethodCoverage(MethodCoverageCriteria.of(packageName,
                className, methodName))
                .orElseGet(() -> {
                    logger.debug("No method coverage found for {}.{}.{}", packageName, className,
                            methodName);
                    return MethodCoverage.EMPTY;
                });
    }

    @Override
    public ClassCoverage getTypeCoverage(ClassCoverageQuery query) {
        String classNameInJacocoFormat = toClassCoverageName(query.getUnit(), query
                .getType(), query
                .getAnonymousClassIndex());
        return coverageRepository.getClassCoverage(ClassCoverageCriteria.of(query.getUnit()
                .getPackageName(), classNameInJacocoFormat)).orElseGet(() -> {
            logger.info("No method coverage found for {}", classNameInJacocoFormat);
            return ClassCoverageImpl.EMPTY;
        });
    }

    private boolean isAbstractOrInterfaceMethod(MethodCoverageQuery query) {
        return query.getMethod().getModifiers().contains(Modifier.ABSTRACT) ||
                ((query.getType() instanceof ClassOrInterfaceDeclaration) && (
                        (ClassOrInterfaceDeclaration) query
                                .getType()).isInterface());
    }

    private String toJacocoMethodNameRegex(MethodCoverageQuery query) {
        final Map<String, String> classGenerics = getClassGenerics(query);
        assert query.getMethod() instanceof MethodDeclaration : "Constructor coverage not implemented yet";
        MethodDeclaration methodDeclaration = (MethodDeclaration) query.getMethod();
        String args = StreamEx.of(methodDeclaration.getParameters()).map(parameter -> {
            StringBuilder name = new StringBuilder();
            if (parameter.getType() instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) parameter.getType();
                if (ref instanceof ClassOrInterfaceType) {
                    ClassOrInterfaceType classType = (ClassOrInterfaceType) ref;
                    if (classType.getScope().isPresent()) {
                        String scopeName = classType.getScope().get().getNameAsString();
                        if (Character.isUpperCase(scopeName.charAt(0))) {
                            name.append(scopeName).append(".");
                        }
                    }
                    name.append(classType.getName());
                }
            }
            if (name.length() == 0) {
                List<String> parts = Splitter.on(".").splitToList(parameter.getType().toString());
                name.append(parts.get(parts.size() - 1));
            }
            String noGenerics = name.toString().replaceAll("<.+>", "").replace("[]", "");
            String fullName = getFullNameFromInnerTypes(query.getType(), noGenerics);
            fullName = getFullNameFromImportTypes(query, fullName);
            Map<String, String> methodGenerics = StreamEx.of(methodDeclaration.getTypeParameters())
                    .filter(typeParameter -> !typeParameter.getTypeBound().isEmpty())
                    .toMap(TypeParameter::getNameAsString, typeParameter -> typeParameter
                            .getTypeBound()
                            .get(0)
                            .getNameAsString());
            fullName = methodGenerics.getOrDefault(fullName, classGenerics.getOrDefault(fullName,
                    fullName));
            StringBuilder result = new StringBuilder(fullName);
            int arrayCount;
            if (parameter.isVarArgs()) {
                result.append("[]");
            } else if ((arrayCount = parameter.getType().getArrayLevel()) != 0) {
                for (int i = 0; i < arrayCount; i++) {
                    result.append("[]");
                }
            } else if (parameter.getType() instanceof ReferenceType) {
                ReferenceType referenceType = (ReferenceType) parameter.getType();
                for (int i = 0; i < referenceType.getArrayLevel(); i++) {
                    result.append("[]");
                }
            }
            return result.toString();
        }).joining(", ");
        return methodDeclaration.getName() + "(" + args + ")";
    }

    private Map<String, String> getClassGenerics(MethodCoverageQuery query) {
        if (query.getType() instanceof ClassOrInterfaceDeclaration) {
            return StreamEx.of(((ClassOrInterfaceDeclaration) query.getType()).getTypeParameters())
                    .toMap(TypeParameter::getNameAsString, typeParameter -> {
                        if (typeParameter.getTypeBound().isEmpty()) {
                            return "Object";
                        }
                        return typeParameter.getTypeBound().get(0).getNameAsString();
                    });
        } else {
            return Collections.emptyMap();
        }
    }

    private String getFullNameFromImportTypes(MethodCoverageQuery query, String fullName) {
        return StreamEx.of(query.getUnit().getCu().getImports())
                .map(ImportDeclaration::getName)
                .select(Name.class)
                .findFirst(nameExpr -> nameExpr.getIdentifier()
                        .equals(fullName) && Character.isUpperCase(nameExpr.getQualifier()
                        .get()
                        .getIdentifier()
                        .charAt(0)))
                .map(Name::asString)
                .orElse(fullName);
    }

    private String getFullNameFromInnerTypes(TypeDeclaration typeDeclaration, String type) {
        TypeDeclaration ancestor = findAncestorSubTypeOf(TypeDeclaration.class, typeDeclaration)
                .orElse(typeDeclaration);
        List<TypeDeclaration> childs = ASTNodeUtils.findChildsSubTypesOf(TypeDeclaration.class,
                ancestor);
        return StreamEx.of(childs)
                .findFirst(td ->
                        td.getNameAsString().equals(type) &&
                                td.getParentNode().isPresent() &&
                                TypeDeclaration.class.isAssignableFrom(td.getParentNode()
                                        .get()
                                        .getClass()))
                .map(subType -> subType.getParentNode()
                        .map(node ->
                                ((TypeDeclaration) node).getNameAsString() + ".")
                        .orElse("") + subType.getNameAsString())
                .orElse(type);
    }

    private static String toClassCoverageName(Unit unit, TypeDeclaration n, int
            anonymousClassIndex) {
        StringBuilder builder = new StringBuilder();
        String parentName = n.getNameAsString();
        if (anonymousClassIndex > 0) {
            builder.append(parentName).append("$").append(anonymousClassIndex);
        } else if (!"".equals(parentName = isNestedClass(unit, n))) {
            builder.append(parentName).append("$").append(n.getName());
        } else {
            builder.append(n.getName());
        }
        String fullName = builder.toString();
        if (fullName.startsWith(COMMON_MAVEN_JAVA_MODULE_PREFIX)) {
            fullName = fullName.substring(COMMON_MAVEN_JAVA_MODULE_PREFIX.length());
        }
        return fullName;
    }

    private static String isNestedClass(Unit unit, TypeDeclaration n) {
        String result = getSubtypeOfUnit(unit, n);
        if ("".equals(result)) {
            result = getSubtypeFromImports(unit, n);
        }
        return result;
    }

    private static String getSubtypeFromImports(Unit unit, TypeDeclaration n) {
        return StreamEx.of(unit.getCu().getImports())
                .map(ImportDeclaration::getName)
                .select(Name.class)
                .findFirst(nameExpr -> nameExpr.getIdentifier().equals(n.getNameAsString()))
                .map(Name::asString)
                .orElse("");
    }

    private static String getSubtypeOfUnit(Unit unit, TypeDeclaration n) {
        return StreamEx.of(unit.getCu().getTypes())
                .findFirst(type -> type.getMembers().contains(n))
                .map(TypeDeclaration::getNameAsString)
                .orElse("");
    }
}
