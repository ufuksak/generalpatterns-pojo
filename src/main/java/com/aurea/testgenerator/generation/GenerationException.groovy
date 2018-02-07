package com.aurea.testgenerator.generation


class GenerationException extends RuntimeException {
    GenerationException() {
        super()
    }

    GenerationException(String message) {
        super(message)
    }

    GenerationException(String message, Throwable cause) {
        super(message, cause)
    }

    GenerationException(Throwable cause) {
        super(cause)
    }
}
