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

        btHistory.add(Pair(timeMillis, bt))

        if (btHistory.size > 40) {
            btHistory.removeAt(0)
        }
    }

    fun predict(): CurvePrediction {

        if (btHistory.size < 2) {
            return CurvePrediction(0.0, 0.0, 0.0, 0.0)
        }

        val last = btHistory.last()
        val prev = btHistory[btHistory.size - 2]

        val dt = (last.first - prev.first) / 1000.0
        val dBt = last.second - prev.second

        val rorValue = if (dt > 0) (dBt / dt) * 60 else 0.0

        val btNow = last.second

        val bt30 = btNow + rorValue * 0.5
        val bt60 = btNow + rorValue * 1.0
        val bt90 = btNow + rorValue * 1.5

        return CurvePrediction(
            bt30,
            bt60,
            bt90,
            rorValue
        )
    }

    fun summary(): String {

        val prediction = predict()

        return "BT+30 ${"%.1f".format(prediction.bt30)}°C  |  " +
               "BT+60 ${"%.1f".format(prediction.bt60)}°C  |  " +
               "BT+90 ${"%.1f".format(prediction.bt90)}°C  |  " +
               "ROR ${"%.1f".format(prediction.ror)}°C/min"
    }
}
