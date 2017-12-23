package com.aurea.methobase

import com.aurea.methobase.meta.ClassMetaInformation
import com.aurea.testgenerator.symbolsolver.SymbolSolver
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import one.util.streamex.StreamEx

import java.util.concurrent.atomic.LongAdder

class TypeVisitor extends VoidVisitorAdapter<Unit> {

    public static final LongAdder COUNTER = new LongAdder()

    List<ClassMetaInformation> metas = []
    SymbolSolver symbolSolver

    TypeVisitor(SymbolSolver symbolSolver) {
        this.symbolSolver = symbolSolver
    }

    @Override
    void visit(ClassOrInterfaceDeclaration n, Unit unit) {
        super.visit(n, unit)
        if (!n.interface) {
            COUNTER.increment()
            metas << new ClassMetaInformation(
                    name: new NameResolver(n).getName(unit),
                    extendedType: getExtendedTypeFullName(n),
                    outerType: getOuterTypeFullName(n, unit),
                    modifiers: n.modifiers.toList(),
                    hasStaticBlocks: !n.findAll(InitializerDeclaration).empty,
                    hasConstructors: !n.findAll(ConstructorDeclaration).empty,
                    isStatic: n.static,
                    isAbstract: n.abstract,
                    locs: new NodeLocCounter().count([n]),
                    uuid: UUID.randomUUID(),
                    filePath: unit.modulePath.toString()
            )
        }
    }

    @Override
    void visit(EnumDeclaration n, Unit unit) {
        super.visit(n, unit)
        COUNTER.increment()
        metas << new ClassMetaInformation(
                name: new NameResolver(n).getName(unit),
                extendedType: getExtendedTypeFullName(n),
                outerType: getOuterTypeFullName(n, unit),
                modifiers: n.modifiers.toList(),
                hasStaticBlocks: !n.findAll(InitializerDeclaration).empty,
                hasConstructors: !n.findAll(ConstructorDeclaration).empty,
                isStatic: n.static,
                isAbstract: false,
                locs: new NodeLocCounter().count([n]),
                uuid: UUID.randomUUID(),
                filePath: unit.modulePath.toString()
        )
    }

    private static String getOuterTypeFullName(TypeDeclaration n, Unit unit) {
        n.getAncestorOfType(TypeDeclaration).map { TypeDeclaration td ->
            new NameResolver(td).getName(unit)
        }.orElse("")
    }

    private String getExtendedTypeFullName(TypeDeclaration td) {
        String result = ''
        if (td instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration coid = td as ClassOrInterfaceDeclaration
            coid.extendedTypes.ifNonEmpty { NodeList<ClassOrInterfaceType> extendedTypes ->
                result = symbolSolver.getParentOf(coid)
                                     .map { it.fullName() }
                                     .or({ tryGetFromImports(td, extendedTypes.first())})
                                     .orElse("")
            }
        }
        result
    }

    private static Optional<String> tryGetFromImports(TypeDeclaration td, ClassOrInterfaceType type) {
        td.getAncestorOfType(CompilationUnit).map { CompilationUnit cu ->
            StreamEx.of(cu.imports).findFirst{it.nameAsString.endsWith('.' + type.nameAsString)}.map {it.nameAsString}.orElse('')
        }
    }

}
