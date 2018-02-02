package com.aurea.testgenerator.extensions

import org.reflections.Reflections


class Extensions {

    static boolean done = false

    synchronized static enable() {
        done = true
        Reflections reflections = new Reflections(ASTExtension.package.name)
        reflections.getSubTypesOf(ASTExtension).each {
            it.newInstance()
        }
    }
}
