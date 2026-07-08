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
 * 値を受信してから [periodMillis] 経過するまで、それ以降の受信を無視するオペレータ.
 *
 * @param periodMillis 無視する期間（ミリ秒）
 */
fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
    require(periodMillis > 0) { "periodMillis must be positive" }
    return throttleFirst(periodMillis.milliseconds, TimeSource.Monotonic)
}

/**
 * 値を受信してから [period] 経過するまで、それ以降の受信を無視するオペレータ.
 *
 * @param period 無視する期間
 */
fun <T> Flow<T>.throttleFirst(period: Duration): Flow<T> {
    require(period.isPositive()) { "period must be positive" }
    return throttleFirst(period, TimeSource.Monotonic)
}

internal fun <T> Flow<T>.throttleFirst(period: Duration, timeSource: TimeSource) = flow {
    var lastTime = timeSource.markNow() - period

    collect { value ->
        if (lastTime.elapsedNow() >= period) {
            lastTime = timeSource.markNow()
            emit(value)
        }
    }
}
