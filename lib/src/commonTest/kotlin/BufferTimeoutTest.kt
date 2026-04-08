package jp.co.gahojin.kflowext

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds

class BufferTimeoutTest : StringSpec({
    "サイズ上限または時間経過でバッファを出力すること" {
        val maxTime = 100.milliseconds
        val maxSize = 3

        val inputFlow = flow {
            // サイズ上限による出力
            emit(1)
            emit(2)
            emit(3) // この時点で、[1,2,3]が出力

            // 時間経過による出力
            emit(4)
            delay(150.milliseconds) // 100msを超過したため、[4]が出力
            emit(5)
            // クローズされるため、[5]が出力
        }

        val result = inputFlow.bufferTimeout(
            maxSize = maxSize,
            maxTime = maxTime,
        ).toList()

        result shouldBe listOf(
            listOf(1, 2, 3),
            listOf(4),
            listOf(5),
        )
    }
})
