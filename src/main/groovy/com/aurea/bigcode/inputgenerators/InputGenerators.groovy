package com.aurea.bigcode.inputgenerators

/**
 * Factory class
 */
class InputGenerators {

    static InputGenerator simple() {
        return new AbsolutelyRandomInputGenerator()
    }
}
