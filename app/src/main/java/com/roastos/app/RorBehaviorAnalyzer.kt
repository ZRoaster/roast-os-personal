package com.roastos.app

data class RorBehavior(
    val crashRisk: Boolean,
    val flickRisk: Boolean,
    val slope: Double,
    val momentum: Double,
    val label: String
)

object RorBehaviorAnalyzer {

    fun analyze(rorHistory: List<Double>): RorBehavior {

        if (rorHistory.size < 3) {
            return RorBehavior(
                crashRisk = false,
                flickRisk = false,
                slope = 0.0,
                momentum = 0.0,
                label = "Not enough data"
            )
        }

        val last = rorHistory.last()
        val prev = rorHistory[rorHistory.size - 2]
        val prev2 = rorHistory[rorHistory.size - 3]

        val slope = last - prev
        val momentum = (last - prev) - (prev - prev2)

        val crashRisk = last < 2.0 && slope < -0.5
        val flickRisk = last > 10.0 && slope > 0.8

        val label = when {
            crashRisk -> "ROR Crash Risk"
            flickRisk -> "ROR Flick Risk"
            slope > 0 -> "ROR Rising"
            slope < 0 -> "ROR Falling"
            else -> "Stable"
        }

        return RorBehavior(
            crashRisk = crashRisk,
            flickRisk = flickRisk,
            slope = slope,
            momentum = momentum,
            label = label
        )
    }
}
