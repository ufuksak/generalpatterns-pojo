package com.aurea.testgenerator.generation.patterns.builder;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import one.util.streamex.StreamEx;

class BuilderTestAnalyzer {

    static List<MethodDeclaration> filterTestable(List<MethodDeclaration> methods) {
        return methods.stream().filter(BuilderTestAnalyzer::hasTestableType).collect(Collectors.toList());
    }

    static Optional<MethodUsage> getCorrespondingGetter(MethodDeclaration builderMethod,
            Set<MethodUsage> pojoMethods) {
        String getter = BuilderTestHelper.buildGetterName(BuilderTestHelper.GET_PREFIX, builderMethod);
        String isGetter = BuilderTestHelper.buildGetterName(BuilderTestHelper.IS_PREFIX, builderMethod);
        return StreamEx.of(pojoMethods)
                .findFirst(pojoMethod -> (pojoMethod.getName().equals(getter)
                        || (pojoMethod.getName().equals(isGetter) && hasBooleanReturnType(pojoMethod)))
                        && haveSameTypes(builderMethod, pojoMethod));
    }

    private static boolean hasTestableType(MethodDeclaration method) {
        if (method.getNameAsString().equals(BuilderTestHelper.BUILD_METHOD)) {
            return false;
        }

        if (method.getParameters().isEmpty() || method.getParameters().size() > 1) {
            return false;
        }

        Type paramType = method.getParameter(0).getType();
        if (BuilderTestHelper.isPrimitive(paramType)) {
            return true;
        }

        ResolvedType resolvedType = paramType.resolve();
        return resolvedType.isReferenceType()
                && resolvedType.asReferenceType().getTypeDeclaration().isClass()
                && !resolvedType.asReferenceType().getTypeDeclaration().isGeneric();
    }

    private static boolean haveSameTypes(MethodDeclaration builderMethod, MethodUsage pojoMethods) {
        String pojoReturnTypeName = normalize(pojoMethods.returnType().describe());
        String builderMethodParamType = builderMethod.getParameter(0).getType().asString();
        return pojoReturnTypeName.equals(builderMethodParamType);
    }

    private static String normalize(String typeName) {
        return typeName.substring(typeName.lastIndexOf(".") + 1);
    }

    private static boolean hasBooleanReturnType(MethodUsage pojoMethod) {
        return normalize(pojoMethod.returnType().describe()).equalsIgnoreCase(Primitive.BOOLEAN.asString());
    }
}
