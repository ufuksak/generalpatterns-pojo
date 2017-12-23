package com.aurea.methobase

import groovy.transform.Canonical

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

@Canonical
class CrawlerConfiguration {

    int numberOfFilesInChunk
    int reportIntervalSeconds
    LongAdder progress
    long fileSizeLimit
    File saveToFolder
    AtomicInteger chunkProgress = new AtomicInteger()
    String metaPrefix = 'unknown'

    static CrawlerConfiguration getDefault() {
        new CrawlerConfiguration(
                numberOfFilesInChunk: 1_000,
                reportIntervalSeconds: 5,
                saveToFolder: new File("metas"),
                fileSizeLimit: 1_000_000
        )
    }

    CrawlerConfiguration forProgressUse(LongAdder progress) {
        this.progress = progress
        this
    }

    CrawlerConfiguration forChunksUse(AtomicInteger chunkProgress) {
        this.chunkProgress = chunkProgress
        this
    }

    CrawlerConfiguration usePrefix(String prefix) {
        this.metaPrefix = prefix
        this
    }

    CrawlerConfiguration saveToFolder(File folder) {
        this.saveToFolder = folder
        this
    }

}
