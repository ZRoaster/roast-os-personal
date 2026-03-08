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

        val calibration = RoastStateModel.calibration

        val calibratedPredFc =
            (predFc + calibration.fcBias).toInt()

        val calibratedPredDrop =
            (predDrop + calibration.dropBias).toInt()

        val baseDev =
            (calibratedPredDrop - calibratedPredFc).coerceAtLeast(45)

        val beanMomentumShift = beanMomentumShift(
            beanBias = calibration.beanBias
        )

        val machineResponseShift = machineResponseShift(
            machineResponseFactor = calibration.machineResponseFactor
        )

        val heatAirShift = controlBiasShift(
            heatBias = calibration.heatBias,
            airBias = calibration.airBias
        )

        // FC 已记录：主要预测 Drop / Dev
        if (actualFc != null) {

            val ror = currentRor ?: 9.0

            val devAdjust = when {
                ror > 10.0 -> -10
                ror > 9.0 -> -5
                ror < 7.0 -> 12
                ror < 8.0 -> 6
                else -> 0
            }

            val calibrationAdjust =
                (-heatAirShift * 2.0).toInt() +
                (-machineResponseShift * 2.0).toInt()

            val predictedDev =
                (baseDev + devAdjust + calibrationAdjust)
                    .coerceIn(45, 150)

            val predictedDrop =
                actualFc + predictedDev

            return CurvePrediction(
                predictedYellowSec = actualYellow ?: predYellow,
                predictedFcSec = actualFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = predictedDev,
                confidence = "High",
                summary = "FC already recorded. Development projection is calibrated by current energy plus learned machine and control response."
            )
        }

        // Yellow 已记录：重点预测 FC / Drop
        if (actualYellow != null) {

            val yellowDelta = actualYellow - predYellow
            val ror = currentRor ?: 13.0

            val fcFromYellowShift =
                (yellowDelta * 0.65).toInt()

            val rorAdjust = when {
                ror > 14.0 -> -18
                ror > 13.0 -> -10
                ror < 10.0 -> 18
                ror < 11.5 -> 10
                else -> 0
            }

            val calibrationAdjust =
                (-beanMomentumShift * 6.0).toInt() +
                (-machineResponseShift * 5.0).toInt() +
                (-heatAirShift * 4.0).toInt()

            val predictedFc =
                (calibratedPredFc + fcFromYellowShift + rorAdjust + calibrationAdjust)
                    .coerceAtLeast(actualYellow + 60)

            val devAdjust = when {
                ror > 14.0 -> -8
                ror < 10.0 -> 10
                else -> 0
            }

            val predictedDev =
                (baseDev + devAdjust + (-heatAirShift * 2.0).toInt())
                    .coerceIn(45, 150)

            val predictedDrop =
                predictedFc + predictedDev

            return CurvePrediction(
                predictedYellowSec = actualYellow,
                predictedFcSec = predictedFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = predictedDev,
                confidence = confidenceForYellow(
                    yellowDelta = yellowDelta,
                    ror = ror,
                    calibration = calibration
                ),
                summary = "Yellow already recorded. FC / Drop projection is corrected by Yellow deviation, current ROR, and learned calibration."
            )
        }

        // Turning 已记录：先推 Yellow，再推 FC / Drop
        if (actualTurning != null) {

            val turningDelta = actualTurning - predTurning

            val calibrationAdjustYellow =
                (beanMomentumShift * 8.0).toInt() +
                (machineResponseShift * 6.0).toInt()

            val predictedYellow =
                (predYellow + (turningDelta * 0.75).toInt() + calibrationAdjustYellow)
                    .coerceAtLeast(actualTurning + 90)

            val calibrationAdjustFc =
                (beanMomentumShift * 10.0).toInt() +
                (machineResponseShift * 8.0).toInt() +
                (heatAirShift * 4.0).toInt()

            val predictedFc =
                (calibratedPredFc + (turningDelta * 0.70).toInt() + calibrationAdjustFc)
                    .coerceAtLeast(predictedYellow + 120)

            val calibrationAdjustDrop =
                (beanMomentumShift * 8.0).toInt() +
                (machineResponseShift * 8.0).toInt() +
                (heatAirShift * 3.0).toInt()

            val predictedDrop =
                (calibratedPredDrop + (turningDelta * 0.70).toInt() + calibrationAdjustDrop)
                    .coerceAtLeast(predictedFc + baseDev)

            return CurvePrediction(
                predictedYellowSec = predictedYellow,
                predictedFcSec = predictedFc,
                predictedDropSec = predictedDrop,
                predictedDevSec = (predictedDrop - predictedFc).coerceAtLeast(45),
                confidence = confidenceForTurning(
                    turningDelta = turningDelta,
                    calibration = calibration
                ),
                summary = "Turning already recorded. Curve projection is estimated from early energy shift and learned machine / bean calibration."
            )
        }

        // 尚未到 Turning：仅使用 Planner 基线 + 校准偏差
        return CurvePrediction(
            predictedYellowSec = predYellow,
            predictedFcSec = calibratedPredFc,
            predictedDropSec = calibratedPredDrop,
            predictedDevSec = (calibratedPredDrop - calibratedPredFc).coerceAtLeast(45),
            confidence = confidenceForBaseline(calibration),
            summary = "No actual anchor recorded yet. Using planner baseline with learned FC / Drop calibration."
        )
    }

    private fun beanMomentumShift(beanBias: Double): Double {
        return beanBias.coerceIn(-2.5, 2.5)
    }

    private fun machineResponseShift(machineResponseFactor: Double): Double {
        return ((machineResponseFactor - 1.0) * 10.0).coerceIn(-2.5, 2.5)
    }

    private fun controlBiasShift(
        heatBias: Double,
        airBias: Double
    ): Double {
        return (heatBias - airBias * 0.6).coerceIn(-3.0, 3.0)
    }

    private fun confidenceForBaseline(
        calibration: RoastStateModel.CalibrationState
    ): String {
        return when {
            calibration.learningCount >= 12 -> "Medium-High"
            calibration.learningCount >= 5 -> "Medium"
            else -> "Baseline"
        }
    }

    private fun confidenceForTurning(
        turningDelta: Int,
        calibration: RoastStateModel.CalibrationState
    ): String {
        return when {
            kotlin.math.abs(turningDelta) <= 6 && calibration.learningCount >= 8 -> "Medium-High"
            kotlin.math.abs(turningDelta) <= 10 -> "Medium"
            else -> "Low-Medium"
        }
    }

    private fun confidenceForYellow(
        yellowDelta: Int,
        ror: Double,
        calibration: RoastStateModel.CalibrationState
    ): String {
        return when {
            kotlin.math.abs(yellowDelta) <= 10 &&
                ror in 11.0..14.0 &&
                calibration.learningCount >= 8 -> "High"
            kotlin.math.abs(yellowDelta) <= 15 -> "Medium-High"
            else -> "Medium"
        }
    }
}
