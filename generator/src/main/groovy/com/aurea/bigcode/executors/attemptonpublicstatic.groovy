package com.aurea.bigcode.executors


import com.aurea.bigcode.TestedMethod
import com.aurea.bigcode.executors.jshell.JShellContext
import com.aurea.bigcode.executors.jshell.JShellMethodExecutor
import com.aurea.bigcode.inputgenerators.InputGenerator
import com.aurea.bigcode.inputgenerators.InputGenerators
import com.aurea.methobase.meta.MethodMetaInformation
import com.aurea.methobase.yaml.YamlMetaInformationRepository
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Stream

import static java.lang.System.exit

if (!args) {
    System.err.println "Please provide name of yml as first arg"
    exit(-1)
}

File yml = new File(args[0])

Stream<MethodMetaInformation> metas = YamlMetaInformationRepository.createForMethods(yml).all()

Collection<MethodMetaInformation> metasWithReturn = metas.filter {
    !it.modifiers.contains(Modifier.NATIVE) &&
    it.returnType != '' && it.returnType != 'void' }.toList()

static boolean sameParameters(MethodDeclaration md, MethodMetaInformation mi) {
    if (md.parameters.size() != mi.parameters.size()) {
        return false
    } else {
        for (int i = 0 ; i < md.parameters.size(); i++) {
            if (md.parameters[i].type.toString() != mi.parameters[i]) {
                return false
            }
        }
    }
    return true
}

metasWithReturn.each { meta ->
    File file = new File("C:/trilogy-group-java/" + meta.filePath)
    CompilationUnit cu = JavaParser.parse(file)
    List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration).findAll {
        it.nameAsString == meta.name && sameParameters(it, meta)
    }
    if (methodDeclarations.size() != 1) {
        println "For $meta.name there are zero or more than one MethodDeclaration"
    } else {
        MethodDeclaration methodDeclaration = methodDeclarations.first()
        if (methodDeclaration.findAll(MethodCallExpr).empty) {
            TestedMethod testedMethod = new TestedMethod(meta, methodDeclaration)
            JShellMethodExecutor executor = new JShellMethodExecutor()

            InputGenerator generator = InputGenerators.simple()
            MethodInput input = generator.next(testedMethod)
            String fullMethodName = testedMethod.fullName()
            String methodExecution = "${fullMethodName}(${input.values.collect{it.snippet}.join(", ")})"

//        println "Executing $methodExecution"
            CompletableFuture<Optional<MethodOutput>> waitForResult = executor.run(testedMethod, JShellContext.ofInput(input))

            try {
                Optional<MethodOutput> maybeResult = waitForResult.get(5L, TimeUnit.SECONDS)
                maybeResult.ifPresentOrElse({ result ->
                    println "${methodExecution}: $result.result of type $result.type"
                }, { println "No result for $methodExecution. Method: \r\n$methodDeclaration\r\n" })

            } catch (TimeoutException te) {
                println "Failed to execute $meta.name within 5 seconds"
            }
        } else {
            println "Skipping method $meta.name . It has method calls: " + methodDeclaration.findAll(MethodCallExpr).collect{it.nameAsString}
        }
    }

}


