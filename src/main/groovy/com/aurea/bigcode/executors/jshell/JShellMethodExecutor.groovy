package com.aurea.bigcode.executors.jshell

import com.aurea.bigcode.TestedMethod
import com.aurea.bigcode.executors.MethodExecutor
import com.aurea.bigcode.executors.MethodOutput
import groovy.util.logging.Log4j2
import jdk.jshell.JShell
import jdk.jshell.JShellException
import jdk.jshell.Snippet
import jdk.jshell.SnippetEvent
import one.util.streamex.StreamEx

import java.util.concurrent.CompletableFuture

@Log4j2
class JShellMethodExecutor implements MethodExecutor<JShellContext> {

    @Override
    CompletableFuture<Optional<MethodOutput>> run(TestedMethod method, JShellContext context) {
        CompletableFuture.supplyAsync {
            JShell shell
            try {
                shell = JShell.create()
                assign(shell, context)

                Optional<MethodOutput> result = act(shell, method, context)

                result
            } catch (IllegalArgumentException | IllegalStateException e) {
                log.error e
            } finally {
                //No shell.withCloseable available in groovy 2.4.12, nor there is a support for try-with-resources
                if (shell) {
                    shell.close()
                }
            }
        }
    }

    static void assign(JShell shell, JShellContext context) {
        context.setup.snippets.each {
            safeSnippetExecution(shell, it)
        }
    }

    static Optional<MethodOutput> act(JShell shell, TestedMethod method, JShellContext context) {
        String methodDeclarationSnippet = method.code
        safeSnippetExecution(shell, methodDeclarationSnippet)
        shell.eval(methodDeclarationSnippet)
        String arguments = "${context.input.values.collect{it.snippet}.join(', ')}"
        String methodName = method.declaration.nameAsString
        String methodCall = "$methodName($arguments)"
        Optional<String> methodCallResult = safeSnippetExecution(shell, methodCall)
        return methodCallResult.map { new MethodOutput(result: it, type: method.declaration.type) }
    }

    static Optional<String> safeSnippetExecution(JShell shell, String code) {
        log.debug "JShell: ${System.lineSeparator()}$code"
        List<SnippetEvent> events = shell.eval(code)
        Optional<JShellException> error = StreamEx.of(events)
                                                  .findFirst { it.status() == Snippet.Status.REJECTED }
                                                  .map { it.exception() }

        if (error.present) {
            throw new IllegalArgumentException("Failed to compile code: $code", error.get())
        }

        StreamEx.of(events)
                .findFirst { it.status() == Snippet.Status.VALID }
                .map { it.value() }
    }
}
