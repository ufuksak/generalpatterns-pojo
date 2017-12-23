package com.aurea.methobase

import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

import java.util.concurrent.atomic.LongAdder

class MethodVisitor extends VoidVisitorAdapter<Unit> {

    public static final LongAdder METHOD_COUNTER = new LongAdder()

    List<MethodMetaInformation> methodMetaInformations = []

    @Override
    void visit(MethodDeclaration n, Unit unit) {
        METHOD_COUNTER.increment()
        methodMetaInformations << new MethodMetaInformation(
                name: n.nameAsString,
                returnType: n.type.toString(),
                genericParameters: n.typeParameters.collect { it.nameAsString },
                modifiers: n.modifiers.toList(),
                thrownExceptions: n.thrownExceptions.collect { it.toString() },
                parameters: n.parameters.collect { it.type.toString() },
                referencedTypes: getAllReferenceTypes(n),
                locs: new NodeLocCounter().count([n]),
                cognitiveComplexity: calculateCognitiveComplexity(n),
                isStatic: n.static,
                isAbstract: n.abstract,
                isPure: isPure(n),
                uuid: UUID.randomUUID(),
                filePath: unit.modulePath.toString()
        )
    }

    private static Set<String> getAllReferenceTypes(MethodDeclaration n) {
        n.findAll(Type).collect { it.toString() }.toSet()
    }

    private static int calculateCognitiveComplexity(MethodDeclaration n) {
        try {
            CognitiveComplexityNodeCalculator.visit(n)
        } catch (Exception e) {
            e.printStackTrace()
            println "Failed on $n"
            return Integer.MAX_VALUE
        }
    }

    private static boolean isPure(MethodDeclaration n) {
        return false
    }
}
