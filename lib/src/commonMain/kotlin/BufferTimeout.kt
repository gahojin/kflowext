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
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、またはmaxTimeが経過するたびに、バッファを返すオペレータ.
 */
fun <T> Flow<T>.bufferTimeout(maxTimeMillis: Long, maxSize: Int = Int.MAX_VALUE): Flow<List<T>> {
    return bufferTimeout(maxTimeMillis.milliseconds, maxSize, TimeSource.Monotonic)
}

/**
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、またはmaxTimeが経過するたびに、バッファを返すオペレータ.
 */
fun <T> Flow<T>.bufferTimeout(maxTime: Duration, maxSize: Int = Int.MAX_VALUE) = bufferTimeout(maxTime, maxSize, TimeSource.Monotonic)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> Flow<T>.bufferTimeout(maxTime: Duration, maxSize: Int = Int.MAX_VALUE, timeSource: TimeSource) = channelFlow {
    val buffer = mutableListOf<T>()
    val upstream = produceIn(this)

    var deadline = timeSource.markNow() + maxTime

    suspend fun flush() {
        if (buffer.isNotEmpty()) {
            send(buffer.toList())
            buffer.clear()
        }
        // deadlineが現在時刻より未来になるまで加算する
        while (deadline.hasPassedNow()) {
            deadline += maxTime
        }
    }

    var isRunning = true
    while (isRunning) {
        val remaining = deadline.elapsedNow()

        select {
            onTimeout(remaining.unaryMinus()) {
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
                    .onClosed {
                        it?.also { throw it }
                        flush()
                        isRunning = false
                    }
            }
        }
    }
}
