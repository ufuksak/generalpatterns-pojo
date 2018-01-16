package com.aurea.bigcode.executors

import com.aurea.ast.common.UnitHelper

import com.aurea.bigcode.TestedMethod
import com.aurea.bigcode.Value
import com.aurea.bigcode.executors.jshell.JShellContext
import com.aurea.bigcode.executors.jshell.JShellMethodExecutor
import com.aurea.methobase.MethodVisitor
import com.aurea.methobase.Unit
import com.aurea.methobase.meta.JavaParserFacadeFactory
import com.aurea.methobase.meta.MethodMetaInformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import spock.lang.Specification

import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

import static java.util.concurrent.TimeUnit.SECONDS

class JShellMethodExecutorSpec extends Specification {
    JShellMethodExecutor executor = new JShellMethodExecutor()

    def "executes simple method in reasonable time"() {
        expect:
        MethodOutput result = runOnCode '''
            int foo() {
                return 5;
            }
        '''

        result.type == PrimitiveType.intType()
        result.result == '5'
    }

    def "modifiers are irrelevant for method execution"() {
        expect:
        MethodOutput result = runOnCode """
            $modifier int foo() {
                return 5;
            }
        """

        result.type == PrimitiveType.intType()
        result.result == '5'

        where:
        modifier    | _
        'static'    | _
        'private'   | _
        'protected' | _
        'public'    | _
    }

    def "methods with different primitive types"() {
        expect:
        MethodOutput result = runOnCode """
            $typeKeyWord foo($typeKeyWord a) {
                return a; 
            }
        """, [value]

        result.type == expectedType
        result.result == expectedValue

        where:
        typeKeyWord | value      | expectedType                | expectedValue
        'int'       | '5'        | PrimitiveType.intType()     | '5'
        'boolean'   | 'true'     | PrimitiveType.booleanType() | 'true'
        'byte'      | "(byte)5"  | PrimitiveType.byteType()    | '5'
        'char'      | "'c'"      | PrimitiveType.charType()    | "'c'"
        'short'     | '(short)5' | PrimitiveType.shortType()   | '5'
        'long'      | '5L'       | PrimitiveType.longType()    | '5'
        'float'     | '5.0f'     | PrimitiveType.floatType()   | '5.0'
        'double'    | '5.0d'     | PrimitiveType.doubleType()  | '5.0'
    }

    def "halting methods can be stopped"() {
        given:
        TestedMethod method = createUTAMethod '''
            int foo() {
                while(1);
            }
        '''
        CompletableFuture<Optional<MethodOutput>> result = executor.run(method, JShellContext.EMPTY)
        !result.completeOnTimeout(Optional.empty(), 5, SECONDS).get().present
    }

    def "milestone 1 case"() {
        expect:
        MethodOutput result = runOnCode """
            int addNumbers(int a, int b) {
                return a + b; 
            }
        """, [a, b]

        result.result == output

        where:
        a   | b   | output
        '3' | '2' | '5'
        '3' | '3' | '6'
        '0' | '7' | '7'
    }


    MethodOutput runOnCode(String methodCode) {
        runOnCode(methodCode, [])
    }

    MethodOutput runOnCode(String methodCode, List<String> inputs) {
        TestedMethod method = createUTAMethod(methodCode)
        MethodInput input = MethodInput.ofValues(inputs.collect { valueOf(it) }.toList())
        CompletableFuture<Optional<MethodOutput>> result = executor.run(method, JShellContext.ofInput(input))
        result.get(10L, SECONDS).get()
    }

    private static TestedMethod createUTAMethod(String methodCode) {
        CompilationUnit cu = UnitHelper.getUnitForMethod(methodCode)
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration).get()
        MethodVisitor visitor = new MethodVisitor(new JavaParserFacadeFactory([]))
        visitor.visit(methodDeclaration, new Unit(cu, UnitHelper.TEST_CLASS_NAME, Paths.get("dummy")))
        MethodMetaInformation meta = visitor.getMethodMetaInformations().first()
        return new TestedMethod(meta, methodDeclaration)
    }

    private static Value valueOf(String snippet) {
        new Value(
                type: new ClassOrInterfaceType("String"),
                value: snippet,
                snippet: snippet
        )
    }
}
