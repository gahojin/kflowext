/*
 * (C) 2026 GAHOJIN, Inc.
 */

package jp.co.gahojin.kflowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * 値が流れてきたら一個だけ流して、そのあと一定時間は流さずに、一定時間経過後にまた流すオペレータ.
 */
fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
    return throttleFirst(periodMillis.milliseconds, TimeSource.Monotonic)
}

/**
 * 値が流れてきたら一個だけ流して、そのあと一定時間は流さずに、一定時間経過後にまた流すオペレータ.
 */
fun <T> Flow<T>.throttleFirst(period: Duration) = throttleFirst(period, TimeSource.Monotonic)

internal fun <T> Flow<T>.throttleFirst(period: Duration, timeSource: TimeSource) = flow {
    var lastTime = timeSource.markNow() - period

    collect { value ->
        if (lastTime.elapsedNow() >= period) {
            lastTime = timeSource.markNow()
            emit(value)
        }
    }
}
