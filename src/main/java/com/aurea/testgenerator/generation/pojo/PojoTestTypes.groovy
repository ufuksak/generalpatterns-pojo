package com.aurea.testgenerator.generation.pojo

import com.aurea.testgenerator.generation.TestType


enum PojoTestTypes implements TestType {
    OPEN_POJO,
    POJO_TESTER,
    POJO_TESTER_GETTER,
    POJO_TESTER_SETTER,
    POJO_TESTER_TO_STRING,
    POJO_TESTER_EQUALS,
    POJO_TESTER_HASH_CODE,
    POJO_TESTER_CONSTRUCTORS
}
