/*
 * (C) 2026 GAHOJIN, Inc.
 */

@file:Suppress("unused")

package jp.co.gahojin.kflowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、または [maxTimeMillis] が経過するたびに、バッファを返すオペレータ.
 *
 * @param maxTimeMillis バッファを放出するまでの最大時間（ミリ秒）
 * @param maxSize バッファの最大サイズ
 */
fun <T> Flow<T>.bufferTimeout(maxTimeMillis: Long, maxSize: Int = Int.MAX_VALUE): Flow<List<T>> {
    require(maxTimeMillis > 0) { "maxTimeMillis must be positive" }
    require(maxSize > 0) { "maxSize must be positive" }
    return bufferTimeout(maxTimeMillis.milliseconds, maxSize, TimeSource.Monotonic)
}

/**
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、または [maxTime] が経過するたびに、バッファを返すオペレータ.
 *
 * @param maxTime バッファを放出するまでの最大時間
 * @param maxSize バッファの最大サイズ
 */
fun <T> Flow<T>.bufferTimeout(maxTime: Duration, maxSize: Int = Int.MAX_VALUE): Flow<List<T>> {
    require(maxTime.isPositive()) { "maxTime must be positive" }
    require(maxSize > 0) { "maxSize must be positive" }
    return bufferTimeout(maxTime, maxSize, TimeSource.Monotonic)
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> Flow<T>.bufferTimeout(
    maxTime: Duration,
    maxSize: Int = Int.MAX_VALUE,
    timeSource: TimeSource,
): Flow<List<T>> = channelFlow {
    var buffer = mutableListOf<T>()
    val upstream = produceIn(this)

    var deadline = timeSource.markNow() + maxTime

    suspend fun flush() {
        if (buffer.isNotEmpty()) {
            val toSend = buffer
            buffer = mutableListOf()
            send(toSend)
        }
        deadline = timeSource.markNow() + maxTime
    }

    var isRunning = true
    while (isRunning) {
        val remaining = (-deadline.elapsedNow()).coerceAtLeast(Duration.ZERO)

        select<Unit> {
            onTimeout(remaining) {
                flush()
            }
            upstream.onReceiveCatching { result ->
                result
                    .onSuccess {
                        buffer.add(it)
                        if (buffer.size >= maxSize) {
                            flush()
                        }
                    }
                    .onClosed { cause ->
                        cause?.let { throw it }
                        if (buffer.isNotEmpty()) {
                            send(buffer)
                        }
                        isRunning = false
                    }
            }
        }
    }
}
