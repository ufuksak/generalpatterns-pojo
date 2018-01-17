package com.aurea.common

import groovy.transform.Memoized
import groovy.util.logging.Log4j2
import org.apache.commons.lang3.time.DurationFormatUtils

import java.time.Duration
import java.time.Instant

@Log4j2
class IterationLogger {

    private static final Duration MAX_INTERVAL_BETWEEN_LOGS = Duration.ofSeconds(60)
    private static final int PROGRESS_STEP = 10

    private long stepsPassed
    private long stepsTowardsNextLog
    private final long stepsTotal
    private final String message
    private Instant lastLogInstant
    private final Instant firstLogInstant

    IterationLogger(long stepsTotal, String message) {
        this.stepsTotal = stepsTotal
        this.message = message
        firstLogInstant = lastLogInstant = Instant.now()
    }

    void iterate() {
        iterate(1)
    }

    void iterate(long steps) {
        stepsPassed += steps
        stepsTowardsNextLog += steps

        if (stepsTowardsNextLog >= logSizeIteration || stepsPassed >= stepsTotal
                || Duration.between(lastLogInstant, Instant.now()) > MAX_INTERVAL_BETWEEN_LOGS) {
            stepsTowardsNextLog %= logSizeIteration
            lastLogInstant = Instant.now()
            reportCurrentState()
        }
    }

    void finish() {
        if (stepsPassed < stepsTotal) {
            stepsPassed = stepsTotal
            reportCurrentState()
        }

        log.info("Time total: {}", formatTime(passedTimed.seconds))
    }

    void reportCurrentState() {
        def estimatedTimeLeft = passedTimed.seconds * (stepsTotal - stepsPassed) / stepsPassed
        String timeLeft = stepsPassed == 0 ? "" : formatTime(estimatedTimeLeft)
        log.info("{}% done. {} of {} {}. Time left: {}",
                String.format("%6.2f", ((double) stepsPassed / stepsTotal) * 100),
                String.format(stepsFormat, stepsPassed), stepsTotal, message, timeLeft)
    }


    private Duration getPassedTimed() {
        Duration.between(firstLogInstant, Instant.now())
    }

    private static String formatTime(BigDecimal seconds) {
        DurationFormatUtils.formatDuration(seconds * 1000 as long, "HH:mm:ss")
    }

    @Memoized
    private String getStepsFormat() {
        int width = stepsTotal > 1 ? Math.floor(Math.log10(stepsTotal)) + 1 : 1
        "%" + width + "d"
    }

    @Memoized
    private long getLogSizeIteration() {
        return stepsTotal / PROGRESS_STEP + 1
    }
}
