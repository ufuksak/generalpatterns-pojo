package com.aurea.testgenerator.template

import com.aurea.testgenerator.pattern.ClassDescription
import com.aurea.testgenerator.pattern.general.HierarchyMatch
import com.aurea.testgenerator.pattern.PatternMatch
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx

class HierarchyCollector implements MatchCollector {

    private int sizeFilter
    private Map<String, String> kidsToParents = new HashMap<>()

    HierarchyCollector(int sizeFilter = 0) {
        this.sizeFilter = sizeFilter
    }

    @Override
    void collect(Map<ClassDescription, List<PatternMatch>> classesToMatches) {
        StreamEx.of(classesToMatches.values())
                .map{it.get(0)}
                .select(HierarchyMatch.class)
                .each {kidsToParents.put(it.declaration, it.parent)}

        Tree tree = new Tree(null, StreamEx.of(kidsToParents.values())
                .distinct()
                .filter { !kidsToParents.containsKey(it) }
                .map { subTree(it) }
                .toList())

        printFlatForest()

        println "Deep 🌲🌲🌲"
        println tree

        println "\nShort 🌲🌲🌲"
        println tree.toStringShort(sizeFilter)
    }

    @TupleConstructor
    private static class Tree {
        String root
        List<Tree> leaves = new ArrayList<>()

        @Memoized
        int size() {
            1 + leaves.stream().mapToInt({it.size()}).sum()
        }

        @Override
        String toString() {
            StreamEx.of(leaves)
                .sortedByInt{-it.size()}
                .map{it.toString()}
                .flatMap{it.readLines().stream()}
                .prepend(root == null ? "" : "$root:")
                .join(System.lineSeparator() + "\t")
        }

        String toStringShort(int sizeFilter) {
            size() < sizeFilter ? "" : StreamEx.of(leaves)
                .sortedByInt{-it.size()}
                .map{it.toStringShort(sizeFilter)}
                .flatMap{it.readLines().stream()}
                .prepend(root == null ? "" : "$root (${size()}):")
                .join(System.lineSeparator() + "\t")
        }
    }

    private Tree subTree(String root) {
        new Tree(root, EntryStream.of(kidsToParents)
            .filter {it.value == root}
            .map{subTree(it.key)}
            .toList())
    }

    private void printFlatForest() {
        Multimap<String, String> forest = ArrayListMultimap.create()
        kidsToParents.each { kid, parent ->
            String ancestor = findAncestor(parent)
            forest.put(ancestor, kid)
        }

        println "Flat 🌲🌲🌲"
        forest.keySet()
                .sort{first, second -> Integer.compare(forest.get(first).size(), forest.get(second).size())}
                .reverse()
                .findAll { forest.get(it).size() > 1}
                .forEach { key ->
            println "$key (${forest.get(key).size()}):"
            forest.get(key).forEach {
                println "\t$it"
            }
        }
    }

    private String findAncestor(String parent) {
        if (!kidsToParents.containsKey(parent)) {
            return parent
        } else {
            String parentOfParent = kidsToParents.get(parent)
            if (parentOfParent == parent) {
                return parent
            } else {
                return findAncestor(parentOfParent)
            }
        }
    }

    @Override
    String toString() {
        return '🌲🌲🌲'
    }
}
