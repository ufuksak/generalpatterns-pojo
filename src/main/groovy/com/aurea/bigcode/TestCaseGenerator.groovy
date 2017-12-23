package com.aurea.bigcode

import com.aurea.bigcode.executors.MethodExecutor
import com.aurea.bigcode.executors.MethodInput
import com.aurea.bigcode.executors.MethodOutput

import com.aurea.bigcode.executors.jshell.JShellContext
import com.aurea.bigcode.executors.jshell.JShellMethodExecutor
import com.aurea.bigcode.inputgenerators.InputGenerator
import com.aurea.bigcode.inputgenerators.InputGenerators
import groovy.util.logging.Log4j2

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Log4j2
class TestCaseGenerator {

    static final int ATTEMPTS = 100

    final int numberOfTests
    final InputGenerator generator
    final MethodExecutor executor

    TestCaseGenerator() {
        //This can be a parameter?
        numberOfTests = 3
        generator = InputGenerators.simple()
        executor = new JShellMethodExecutor()
    }

    List<TestCase> generate(TestedMethod testedMethod) {
        int attempt = 0
        List<TestCase> testCases = []

        while (testCases.size() != numberOfTests && attempt++ < ATTEMPTS) {
            MethodInput input = generator.next(testedMethod)
            JShellContext jShellContext = JShellContext.ofInput(input)
            CompletableFuture<Optional<MethodOutput>> execution = executor.run(testedMethod, jShellContext)
            try {
                Optional<MethodOutput> output = execution.get(5, TimeUnit.SECONDS)
                output.ifPresent {
                    testCases << new TestCase(input, it)
                }
            } catch (TimeoutException te) {
                String methodExecution = "${testedMethod.fullName()}(${input.values.collect{it.snippet}.join(", ")})"
                log.error "Failed to execute $methodExecution. Reason: Timeout", te
            }
        }

        if (testCases.size() != numberOfTests) {
            throw new IllegalArgumentException("Failed to generate test for ${testedMethod.fullName()}, " +
                    "only ${testCases.size()} out of $numberOfTests test cases found.")
        }

        testCases
    }
}
