package com.aurea.methobase

import groovy.io.FileType

import java.util.concurrent.atomic.LongAdder

class JavaCollector {

    static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please provide root folder(s) of java files")
        }
        File javaFilesList = new File("java-files-list.txt")
        List<File> javaFiles = []

        args[0].split(';').each { root ->
            File rootDir = new File(root)
            println "Crawling in $root"
            List<File> files = []
            LongAdder progress = new LongAdder()

            rootDir.eachFileRecurse FileType.FILES, {
                if (it.name.endsWith('.java')) {
                    files << it
                    progress.increment()
                    if (progress.intValue() % 1000 == 0) {
                        println "Found ${progress.intValue()} javas"
                    }
                }
            }
            println "Found ${files.size()} Java in $root"
            javaFiles.addAll(files)
        }

        javaFilesList.write(javaFiles.collect { it.absolutePath }.join(System.lineSeparator()))
    }
}
