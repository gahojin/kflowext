package jp.co.gahojin.kflowext

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class ThrottleFirstTest : StringSpec({
    "指定された期間内のイベントを間引くこと" {
        val period = 100.milliseconds

        val inputFlow = flow {
            emit("A") // 最初の要素は出力される

            delay(50.milliseconds)
            emit("B") // 100ms経っていないため、間引かれる

            delay(60.milliseconds)
            emit("C") // 50+60=110msとなり、100ms経っているため、出力される

            delay(40.milliseconds)
            emit("D") // 50+60+40=150ms、間引かれる

            delay(100.milliseconds)
            emit("E") // 50+60+40+100=250ms、出力される
        }

        // 実行
        val result = inputFlow.throttleFirst(period).toList()

        // 検証
        result shouldBe listOf("A", "C", "E")
    }
})
