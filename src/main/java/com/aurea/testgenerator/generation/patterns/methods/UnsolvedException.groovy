package com.aurea.testgenerator.generation.patterns.methods

import com.github.javaparser.ast.Node


class UnsolvedException extends RuntimeException {
    Node unsolvedNode

    UnsolvedException(Node unsolvedNode) {
        super()
        this.unsolvedNode = unsolvedNode
    }

    UnsolvedException(Node unsolvedNode, String message) {
        super(message)
        this.unsolvedNode = unsolvedNode
    }

    UnsolvedException(Node unsolvedNode, String message, Throwable cause) {
        super(message, cause)
        this.unsolvedNode = unsolvedNode
    }

    UnsolvedException(Node unsolvedNode, Throwable cause) {
        super(cause)
        this.unsolvedNode = unsolvedNode
    }
}
