package com.roastos.app

data class CurvePrediction(
    val predictedBt30: Double,
    val predictedBt60: Double,
    val predictedBt90: Double,
    val predictedFcTime: Double?,
    val predictedRor: Double,
    val deviation: String
)

object RoastCurveEngineV2 {

    private val btHistory = mutableListOf<Pair<Long, Double>>()

    fun reset() {
        btHistory.clear()
    }

    fun record(bt: Double, time: Long) {
        btHistory.add(Pair(time, bt))

        if (btHistory.size > 30) {
            btHistory.removeAt(0)
        }
    }

    fun predict(): CurvePrediction {

        if (btHistory.size < 2) {
            return CurvePrediction(
                0.0, 0.0, 0.0, null, 0.0, "Not enough data"
            )
        }

        val last = btHistory.last()
        val prev = btHistory[btHistory.size - 2]

        val dt = (last.first - prev.first).toDouble() / 1000.0
        val dbt = last.second - prev.second

        val ror = if (dt > 0) dbt / dt * 60 else 0.0

        val btNow = last.second

        val bt30 = btNow + ror * 0.5
        val bt60 = btNow + ror * 1.0
        val bt90 = btNow + ror * 1.5

        val fcTarget = 198.0

        val timeToFc = if (ror > 0) {
            ((fcTarget - btNow) / ror) * 60
        } else null

        val deviation = when {
            ror < 3 -> "Crash Risk"
            ror < 5 -> "Low Momentum"
            ror > 18 -> "Runaway Heat"
            ror > 14 -> "Too Fast"
            else -> "Normal"
        }

        return CurvePrediction(
            bt30,
            bt60,
            bt90,
            timeToFc,
            ror,
            deviation
        )
    }
}
