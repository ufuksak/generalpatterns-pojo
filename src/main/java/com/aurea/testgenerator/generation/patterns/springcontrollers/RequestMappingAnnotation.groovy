package com.aurea.testgenerator.generation.patterns.springcontrollers

import javax.swing.text.html.Option

enum RequestMappingAnnotation {

    REQUEST_MAPPING("RequestMapping", null),
    GET_MAPPING("GetMapping", "get"),
    POST_MAPPING("PostMapping", "post"),
    PUT_MAPPING("PutMapping", "put"),
    PATCH_MAPPING("PatchMapping", "patch"),
    DELETE_MAPPING("DeleteMapping", "delete")

    String name
    String method

    RequestMappingAnnotation(String name, String method){
        this.name = name
        this.method = method
    }

    static RequestMappingAnnotation of(String annotationName){
        Optional.ofNullable(values().find{it.name == annotationName})
            .orElseThrow{new IllegalArgumentException("${annotationName} is not a Spring request mapping annotation")}
    }

    static Set<String> names(){
        values().collect {it.name}.toSet()
    }

 }