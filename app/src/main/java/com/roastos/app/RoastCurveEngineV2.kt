package com.roastos.app

data class RoastCurvePredictionV2(
    val bt30: Double,
    val bt60: Double,
    val bt90: Double,
    val ror: Double
)

object RoastCurveEngineV2 {

    private val btHistory = mutableListOf<Pair<Long, Double>>()

    fun reset() {
        btHistory.clear()
    }

    fun record(
        bt: Double,
        timeMillis: Long
    ) {
        btHistory.add(Pair(timeMillis, bt))

        if (btHistory.size > 40) {
            btHistory.removeAt(0)
        }
    }

    fun predict(): RoastCurvePredictionV2 {
        if (btHistory.size < 2) {
            return RoastCurvePredictionV2(
                bt30 = 0.0,
                bt60 = 0.0,
                bt90 = 0.0,
                ror = 0.0
            )
        }

        val last = btHistory.last()
        val prev = btHistory[btHistory.size - 2]

        val dtSec = (last.first - prev.first).toDouble() / 1000.0
        val deltaBt = last.second - prev.second

        val rorPerMin = if (dtSec > 0.0) {
            (deltaBt / dtSec) * 60.0
        } else {
            0.0
        }

        val btNow = last.second

        val bt30 = btNow + rorPerMin * 0.5
        val bt60 = btNow + rorPerMin * 1.0
        val bt90 = btNow + rorPerMin * 1.5

        return RoastCurvePredictionV2(
            bt30 = bt30,
            bt60 = bt60,
            bt90 = bt90,
            ror = rorPerMin
        )
    }

    fun summary(): String {
        val prediction = predict()

        return """
Curve Prediction

BT +30s
${"%.1f".format(prediction.bt30)} ℃

BT +60s
${"%.1f".format(prediction.bt60)} ℃

BT +90s
${"%.1f".format(prediction.bt90)} ℃

ROR
${"%.1f".format(prediction.ror)} ℃/min
        """.trimIndent()
    }
}
