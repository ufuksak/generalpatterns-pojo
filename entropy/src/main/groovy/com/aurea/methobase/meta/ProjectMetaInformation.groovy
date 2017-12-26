package com.aurea.methobase.meta

import groovy.transform.Canonical


@Canonical
class ProjectMetaInformation extends MetaInformation {
    List<ModuleMetaInformation> modules
}
