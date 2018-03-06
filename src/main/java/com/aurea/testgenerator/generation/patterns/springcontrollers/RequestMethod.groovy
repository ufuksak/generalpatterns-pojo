package com.aurea.testgenerator.generation.patterns.springcontrollers


enum RequestMethod {
    GET("get"), POST("post"), PUT("put"), PATCH(), DELETE

    String method

    RequestMethod(String method) {
        this.method = method
    }
}
