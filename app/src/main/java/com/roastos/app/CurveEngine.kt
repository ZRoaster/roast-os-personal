package com.roastos.app

data class CurvePrediction(
    val predictedYellowSec: Int,
    val predictedFcSec: Int,
    val predictedDropSec: Int,
    val predictedDevSec: Int,
    val confidence: String,
    val summary: String
)

object CurveEngine {

    fun predict(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        currentRor: Double?
    ): CurvePrediction {

        val baseDev = (predDrop - predFc).coerceAtLeast(45)

        // 已经到 FC：只需要预测 Drop
        if (actualFc != null) {
            val ror = currentRor ?: 9.0

            val devAdjust = when {
                ror > 10.0 -> -10
                ror > 9.0 -> -5
                ror < 7.0 -> 12
                ror < 8.0 -> 6
                else -> 0
            }

            val predictedDev = (baseDev + devAdjust).coerceIn(45, 150)
            val predictedDrop = actualFc + predictedDev

            return CurvePrediction(
                predictedYellowSec = actualYellow ?: predYellow,
                predictedFcSec = actualFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = predictedDev,
                confidence = "High",
                summary = "FC already recorded. Development projection is driven mainly by current pre-FC energy."
            )
        }

        // 已经到 Yellow：重点预测 FC / Drop
        if (actualYellow != null) {
            val yellowDelta = actualYellow - predYellow
            val ror = currentRor ?: 13.0

            val fcFromYellowShift = (yellowDelta * 0.65).toInt()

            val rorAdjust = when {
                ror > 14.0 -> -18
                ror > 13.0 -> -10
                ror < 10.0 -> 18
                ror < 11.5 -> 10
                else -> 0
            }

            val predictedFc = (predFc + fcFromYellowShift + rorAdjust)
                .coerceAtLeast(actualYellow + 60)

            val devAdjust = when {
                ror > 14.0 -> -8
                ror < 10.0 -> 10
                else -> 0
            }

            val predictedDev = (baseDev + devAdjust).coerceIn(45, 150)
            val predictedDrop = predictedFc + predictedDev

            return CurvePrediction(
                predictedYellowSec = actualYellow,
                predictedFcSec = predictedFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = predictedDev,
                confidence = "Medium-High",
                summary = "Yellow already recorded. FC / Drop projection is corrected by Yellow deviation and current ROR."
            )
        }

        // 已经到 Turning：先推 Yellow，再推 FC / Drop
        if (actualTurning != null) {
            val turningDelta = actualTurning - predTurning

            val predictedYellow = (predYellow + (turningDelta * 0.75).toInt())
                .coerceAtLeast(actualTurning + 90)

            val predictedFc = (predFc + (turningDelta * 0.70).toInt())
                .coerceAtLeast(predictedYellow + 120)

            val predictedDrop = (predDrop + (turningDelta * 0.70).toInt())
                .coerceAtLeast(predictedFc + baseDev)

            return CurvePrediction(
                predictedYellowSec = predictedYellow,
                predictedFcSec = predictedFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = (predictedDrop - predictedFc).coerceAtLeast(45),
                confidence = "Medium",
                summary = "Turning already recorded. Curve projection is estimated from early energy shift."
            )
        }

        // 还没到 Turning：只能返回基准预测
        return CurvePrediction(
            predictedYellowSec = predYellow,
            predictedFcSec = predFc,
            predictedDropSec = predDrop,
            predictedDevSec = baseDev,
            confidence = "Baseline",
            summary = "No actual anchor recorded yet. Using planner baseline only."
        )
    }
}
