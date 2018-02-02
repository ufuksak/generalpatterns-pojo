package com.aurea.testgenerator.generation

import one.util.streamex.StreamEx
import spock.lang.Specification


class UnitTestMergeEngineSpec extends Specification {

    def "foo"() {
        expect:
        List<Foo> foos = [
                new Foo(texts: ["a", "b"]),
                new Foo(texts: ["c", "d"]),
                new Foo(texts: ["e", "f"])
        ]

    }

    class Foo {
        List<String> texts
    }

}
