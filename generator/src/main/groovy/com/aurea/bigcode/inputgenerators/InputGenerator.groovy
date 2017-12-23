package com.aurea.bigcode.inputgenerators


import com.aurea.bigcode.TestedMethod
import com.aurea.bigcode.executors.MethodInput

interface InputGenerator {

    MethodInput next(TestedMethod method)
}
