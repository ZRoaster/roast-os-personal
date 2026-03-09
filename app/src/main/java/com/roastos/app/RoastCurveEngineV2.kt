package com.roastos.app

data class CurvePrediction(
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

    fun record(bt: Double, timeMillis: Long) {

        btHistory.add(timeMillis to bt)

        if (btHistory.size > 30) {
            btHistory.removeAt(0)
        }
    }

    fun predict(): CurvePrediction {

        if (btHistory.size < 2) {
            return CurvePrediction(
                bt30 = 0.0,
                bt60 = 0.0,
                bt90 = 0.0,
                ror = 0.0
            )
        }

        val last = btHistory.last()
        val prev = btHistory[btHistory.size - 2]

        val dt = (last.first - prev.first).toDouble() / 1000.0
        val dBt = last.second - prev.second

        val ror = if (dt > 0) (dBt / dt) * 60 else 0.0

        val btNow = last.second

        val bt30 = btNow + ror * 0.5
        val bt60 = btNow + ror * 1.0
        val bt90 = btNow + ror * 1.5

        return CurvePrediction(
            bt30 = bt30,
            bt60 = bt60,
            bt90 = bt90,
            ror = ror
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
