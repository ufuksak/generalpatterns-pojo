package com.aurea.methobase

import com.aurea.methobase.meta.JavaParserFacadeFactory
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.meta.purity.IsPureFunction
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade

import java.util.concurrent.atomic.LongAdder

class MethodVisitor extends VoidVisitorAdapter<Unit> {

    public static final LongAdder METHOD_COUNTER = new LongAdder()

    List<MethodMetaInformation> methodMetaInformations = []
    JavaParserFacadeFactory javaParserFacadeFactory

    MethodVisitor(JavaParserFacadeFactory javaParserFacadeFactory) {
        this.javaParserFacadeFactory = javaParserFacadeFactory
    }

    @Override
    void visit(MethodDeclaration n, Unit unit) {
        METHOD_COUNTER.increment()
        String projectName = unit.modulePath.getName(0)
        JavaParserFacade facade = javaParserFacadeFactory.fromProjectName(projectName)
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
                isPure: isPure(n, facade),
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

    private static boolean isPure(MethodDeclaration n, JavaParserFacade solver) {
        new IsPureFunction(solver).test(n)
    }
}
