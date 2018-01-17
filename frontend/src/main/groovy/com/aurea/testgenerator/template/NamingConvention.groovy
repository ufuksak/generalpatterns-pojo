package com.aurea.testgenerator.template

import groovy.transform.Canonical

@Canonical
class NamingConvention {
    String accessorConstant = 'alwaysReturnsConstant'
    String accessorNull = 'alwaysReturnsNull'
    String accessorSetValue = 'returnsSetValue'
    String enumPreposition = 'for'
}
