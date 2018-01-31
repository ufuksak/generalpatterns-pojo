package com.aurea.testgenerator.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldResolver {

    private static final Map<ResolvedPrimitiveType, PrimitiveType> PRIMITIVES =  ImmutableMap.<ResolvedPrimitiveType, com.github.javaparser.ast.type.PrimitiveType>builder()
            .put(ResolvedPrimitiveType.BYTE, com.github.javaparser.ast.type.PrimitiveType.byteType())
            .put(ResolvedPrimitiveType.SHORT, com.github.javaparser.ast.type.PrimitiveType.shortType())
            .put(ResolvedPrimitiveType.CHAR, com.github.javaparser.ast.type.PrimitiveType.charType())
            .put(ResolvedPrimitiveType.INT, com.github.javaparser.ast.type.PrimitiveType.intType())
            .put(ResolvedPrimitiveType.LONG, com.github.javaparser.ast.type.PrimitiveType.longType())
            .put(ResolvedPrimitiveType.BOOLEAN, com.github.javaparser.ast.type.PrimitiveType.booleanType())
            .put(ResolvedPrimitiveType.FLOAT, com.github.javaparser.ast.type.PrimitiveType.floatType())
            .put(ResolvedPrimitiveType.DOUBLE, com.github.javaparser.ast.type.PrimitiveType.doubleType())
            .build();

    private final JavaParserFacade facade;

    public FieldResolver(JavaParserFacade facade) {
        this.facade = facade;
    }

    public List<com.github.javaparser.ast.body.FieldDeclaration> getAllAccessibleFields(ClassOrInterfaceDeclaration
                                                                                                coid) {
        List<com.github.javaparser.ast.body.FieldDeclaration> result = new ArrayList<>();
        result.addAll(coid.getFields());
        if (!coid.getExtendedTypes().isEmpty()) {
            String fullName = ASTNodeUtils.getFullName(coid);
            ResolvedReferenceTypeDeclaration typeReference = facade.getTypeSolver().solveType(fullName);
            for (ResolvedReferenceType referenceType : typeReference.getAllAncestors()) {
                ResolvedReferenceTypeDeclaration type = referenceType.getTypeDeclaration();
                getAllAccessibleFields(type, result);
            }
        }
        return result;
    }

    private void getAllAccessibleFields(ResolvedReferenceTypeDeclaration typeDeclaration, List<com.github.javaparser.ast.body
            .FieldDeclaration> result) {
        result.addAll(typeDeclaration.getAllFields().stream().map(this::asFieldDeclaration).collect(Collectors.toList
                ()));
        for (ResolvedReferenceType ancestor : typeDeclaration.getAllAncestors()) {
            getAllAccessibleFields(ancestor.getTypeDeclaration(), result);
        }
    }

    private com.github.javaparser.ast.body.FieldDeclaration asFieldDeclaration(ResolvedFieldDeclaration fieldDeclaration) {
        return new com.github.javaparser.ast.body.FieldDeclaration(
                convertToModifiers(fieldDeclaration),
                convertToVariableDeclarator(fieldDeclaration));
    }

    private com.github.javaparser.ast.type.Type mapType(ResolvedType type) {
        if (type instanceof ResolvedReferenceType) {
            return new ClassOrInterfaceType(type.asReferenceType().getQualifiedName());
        } else if (type instanceof ResolvedPrimitiveType) {
            return PRIMITIVES.get(type.asPrimitive());
        } else if (type instanceof ResolvedArrayType) {
            ResolvedArrayType arrayType = type.asArrayType();
            return new com.github.javaparser.ast.type.ArrayType(mapType(arrayType.getComponentType()));
        }
        throw new IllegalArgumentException("Don't know what to do with " + type);
    }

    private VariableDeclarator convertToVariableDeclarator(ResolvedFieldDeclaration fieldDeclaration) {
        ResolvedType type = fieldDeclaration.getType();
        return new VariableDeclarator(mapType(type), fieldDeclaration.getName());
    }

    private EnumSet<Modifier> convertToModifiers(ResolvedFieldDeclaration fieldDeclaration) {
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        switch (fieldDeclaration.accessSpecifier()) {
            case PUBLIC:
                modifiers.add(Modifier.PUBLIC);
                break;
            case PRIVATE:
                modifiers.add(Modifier.PRIVATE);
                break;
            case PROTECTED:
                modifiers.add(Modifier.PROTECTED);
                break;
            case DEFAULT:
                break;
        }

        if (fieldDeclaration.isStatic()) {
            modifiers.add(Modifier.STATIC);
        }
        return modifiers;
    }
}
