package com.aurea.testgenerator.source.structure

import groovy.transform.Canonical


@Canonical
class Project {
    List<Module> modules = []
}
