package com.roastos.app

data class CurvePrediction(
    val predictedBt30: Double,
    val predictedBt60: Double,
    val predictedBt90: Double,
    val predictedFcTimeSec: Double?,
    val predictedRor: Double,
    val deviation: String
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
        btHistory.add(timeMillis to bt)

        if (btHistory.size > 30) {
            btHistory.removeAt(0)
        }
    }

    fun predict(): CurvePrediction {
        if (btHistory.size < 2) {
            return CurvePrediction(
                predictedBt30 = 0.0,
                predictedBt60 = 0.0,
                predictedBt90 = 0.0,
                predictedFcTimeSec = null,
                predictedRor = 0.0,
                deviation = "Not enough data"
            )
        }

        val last = btHistory.last()
        val prev = btHistory[btHistory.size - 2]

        val dtSec = (last.first - prev.first).toDouble() / 1000.0
        val deltaBt = last.second - prev.second

        val rorPerMin = if (dtSec > 0.0) {
            deltaBt / dtSec * 60.0
        } else {
            0.0
        }

        val btNow = last.second

        val predictedBt30 = btNow + rorPerMin * 0.5
        val predictedBt60 = btNow + rorPerMin * 1.0
        val predictedBt90 = btNow + rorPerMin * 1.5

        val fcTargetBt = 198.0

        val predictedFcTimeSec = if (rorPerMin > 0.0 && btNow < fcTargetBt) {
            ((fcTargetBt - btNow) / rorPerMin) * 60.0
        } else {
            null
        }

        val deviation = when {
            rorPerMin < 3.0 -> "Crash Risk"
            rorPerMin < 5.0 -> "Low Momentum"
            rorPerMin > 18.0 -> "Runaway Heat"
            rorPerMin > 14.0 -> "Too Fast"
            else -> "Normal"
        }

        return CurvePrediction(
            predictedBt30 = predictedBt30,
            predictedBt60 = predictedBt60,
            predictedBt90 = predictedBt90,
            predictedFcTimeSec = predictedFcTimeSec,
            predictedRor = rorPerMin,
            deviation = deviation
        )
    }

    fun summary(): String {
        val p = predict()

        return """
Curve Prediction

BT +30s
${"%.1f".format(p.predictedBt30)}℃

BT +60s
${"%.1f".format(p.predictedBt60)}℃

BT +90s
${"%.1f".format(p.predictedBt90)}℃

Predicted ROR
${"%.1f".format(p.predictedRor)}℃/min

Predicted FC
${p.predictedFcTimeSec?.let { "%.0f".format(it) + "s" } ?: "-"}

Deviation
${p.deviation}
        """.trimIndent()
    }
}
