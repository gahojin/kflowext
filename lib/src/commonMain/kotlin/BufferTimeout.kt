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

/**
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、またはmaxTimeが経過するたびに、バッファを返すオペレータ.
 */
fun <T> Flow<T>.bufferTimeout(maxSize: Int = Int.MAX_VALUE, maxTimeMillis: Long): Flow<List<T>> {
    return bufferTimeout(maxSize, maxTimeMillis.milliseconds)
}

/**
 * 受信した値をバッファに収集し、バッファが最大サイズに達するか、またはmaxTimeが経過するたびに、バッファを返すオペレータ.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.bufferTimeout(maxSize: Int = Int.MAX_VALUE, maxTime: Duration) = channelFlow {
    val buffer = mutableListOf<T>()
    val upstream = produceIn(this)

    val onWindowClosed = suspend {
        if (buffer.isNotEmpty()) {
            send(buffer.toList())
            buffer.clear()
        }
    }

    var isRunning = true
    while (isRunning) {
        select {
            upstream.onReceiveCatching { result ->
                result
                    .onSuccess {
                        buffer.add(it)
                        if (buffer.size >= maxSize) {
                            onWindowClosed()
                        }
                    }
                    .onClosed {
                        it?.also { throw it }
                        onWindowClosed()
                        isRunning = false
                    }
            }
            onTimeout(maxTime) {
                onWindowClosed()
            }
        }
    }
}
