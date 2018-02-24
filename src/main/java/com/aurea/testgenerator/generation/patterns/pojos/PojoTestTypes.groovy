package com.aurea.testgenerator.generation.patterns.pojos

import com.aurea.testgenerator.generation.TestType


enum PojoTestTypes implements TestType {
    OPEN_POJO,
    OPEN_POJO_GETTER,
    OPEN_POJO_SETTER,
    OPEN_POJO_TO_STRING,
    OPEN_POJO_EQUALS,
    OPEN_POJO_HASH_CODE,
    OPEN_POJO_CONSTRUCTORS,
    POJO_TESTER,
    POJO_TESTER_GETTER,
    POJO_TESTER_SETTER,
    POJO_TESTER_TO_STRING,
    POJO_TESTER_EQUALS,
    POJO_TESTER_HASH_CODE,
    POJO_TESTER_CONSTRUCTORS
}
