package com.aurea.bigcode.executors

import com.aurea.bigcode.executors.context.Context
import com.aurea.bigcode.TestedMethod

import java.util.concurrent.CompletableFuture

interface MethodExecutor<T extends Context> {

    CompletableFuture<Optional<MethodOutput>> run(TestedMethod method, T context)
}
