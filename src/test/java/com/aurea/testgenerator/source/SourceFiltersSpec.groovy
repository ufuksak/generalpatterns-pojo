package com.aurea.testgenerator.source

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


class SourceFiltersSpec extends Specification {

    @Rule
    final TemporaryFolder folder = new TemporaryFolder()

    def "can find a test if it has same relative path"() {
        setup:
        FileTreeBuilder fileTree = new FileTreeBuilder(folder.root)
        fileTree.dir('project-potato') {
            dir('main') {
                dir('src') {
                    dir('example') {
                        file('Foo.java')
                        file('NotFoo.java')
                    }
                }
            }
            dir('test') {
                dir('src') {
                    dir('example') {
                        file('FooTest.java')
                    }
                }
            }
        }

        when:
        def predicate = SourceFilters.hasTest(
                folder.root.toPath().resolve("project-potato/main"),
                folder.root.toPath().resolve("project-potato/test"))

        then:
        predicate.test(folder.root.toPath().resolve('project-potato/main/src/example/Foo.java'))
        !predicate.test(folder.root.toPath().resolve('project-potato/main/src/example/NotFoo.java'))
    }
}
