package com.roastos.app

import kotlin.math.abs
import kotlin.math.max

data class CurvePoint(
    val timeSec: Int,
    val bt: Double,
    val ror: Double,
    val phase: String,
    val isActual: Boolean = false
)

data class CurveAnchor(
    val label: String,
    val timeSec: Int,
    val isActual: Boolean
)

data class CurveDeviation(
    val label: String,
    val deltaSec: Int,
    val severity: String
)

data class RoastCurveResult(
    val predictedPoints: List<CurvePoint>,
    val actualPoints: List<CurvePoint>,
    val anchors: List<CurveAnchor>,
    val deviations: List<CurveDeviation>,
    val summary: String
)

object RoastCurveEngine {

    fun buildFromCurrentState(): RoastCurveResult {
        val planner = AppState.lastPlannerResult
        val timeline = RoastTimelineStore.current

        val predTurning = timeline.predicted.turningSec
            ?: planner?.let { (it.h1Sec - 60.0).toInt().coerceAtLeast(50) }
            ?: 80

        val predYellow = timeline.predicted.yellowSec
            ?: planner?.h2Sec?.toInt()
            ?: 250

        val predFc = timeline.predicted.fcSec
            ?: planner?.fcPredSec?.toInt()
            ?: 480

        val predDrop = timeline.predicted.dropSec
            ?: planner?.dropSec?.toInt()
            ?: 570

        val actualTurning = timeline.actual.turningSec
        val actualYellow = timeline.actual.yellowSec
        val actualFc = timeline.actual.fcSec
        val actualDrop = timeline.actual.dropSec

        val predictedFinalSec = max(predDrop, 300)
        val actualFinalSec = max(
            actualDrop ?: 0,
            max(
                actualFc ?: 0,
                max(
                    actualYellow ?: 0,
                    actualTurning ?: 0
                )
            )
        )

        val finalSec = max(predictedFinalSec, actualFinalSec).coerceAtLeast(300)

        val predictedBtSeries = buildPredictedBtSeries(
            finalSec = finalSec,
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val predictedRorSeries = buildControlledRor(predictedBtSeries)

        val predictedPoints = predictedBtSeries.indices.map { i ->
            CurvePoint(
                timeSec = i,
                bt = predictedBtSeries[i],
                ror = predictedRorSeries.getOrElse(i) { 0.0 },
                phase = detectPhaseAtTime(i, predTurning, predYellow, predFc, predDrop),
                isActual = false
            )
        }

        val actualPoints =
            if (actualTurning != null || actualYellow != null || actualFc != null || actualDrop != null) {
                val aTurning = actualTurning ?: predTurning
                val aYellow = actualYellow ?: predYellow
                val aFc = actualFc ?: predFc
                val aDrop = actualDrop ?: predDrop

                val actualBtSeries = buildActualBtSeries(
                    finalSec = finalSec,
                    actualTurning = aTurning,
                    actualYellow = aYellow,
                    actualFc = aFc,
                    actualDrop = aDrop
                )

                val actualRorSeries = buildControlledRor(actualBtSeries)

                actualBtSeries.indices.map { i ->
                    CurvePoint(
                        timeSec = i,
                        bt = actualBtSeries[i],
                        ror = actualRorSeries.getOrElse(i) { 0.0 },
                        phase = detectPhaseAtTime(i, aTurning, aYellow, aFc, aDrop),
                        isActual = true
                    )
                }
            } else {
                emptyList()
            }

        val anchors = buildAnchors(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = actualTurning,
            actualYellow = actualYellow,
            actualFc = actualFc,
            actualDrop = actualDrop
        )

        val deviations = buildDeviations(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = actualTurning,
            actualYellow = actualYellow,
            actualFc = actualFc,
            actualDrop = actualDrop
        )

        val summary = """
Curve Engine v1.3

Predicted Points ${predictedPoints.size}
Actual Points ${actualPoints.size}

Predicted Anchors
Turning ${predTurning}s
Yellow ${predYellow}s
FC ${predFc}s
Drop ${predDrop}s

Actual Anchors
Turning ${actualTurning?.toString() ?: "-"}
Yellow ${actualYellow?.toString() ?: "-"}
FC ${actualFc?.toString() ?: "-"}
Drop ${actualDrop?.toString() ?: "-"}

Deviation Summary
${buildDeviationSummaryText(deviations)}

Control Precision
ROR smoothed with weighted moving average
Predicted / Actual curve separation enabled
Phase tagging enabled
Deviation severity enabled
        """.trimIndent()

        return RoastCurveResult(
            predictedPoints = predictedPoints,
            actualPoints = actualPoints,
            anchors = anchors,
            deviations = deviations,
            summary = summary
        )
    }

    private fun buildPredictedBtSeries(
        finalSec: Int,
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): List<Double> {
        val result = mutableListOf<Double>()

        for (t in 0..finalSec) {
            result.add(
                estimateBt(
                    timeSec = t,
                    turningSec = predTurning,
                    yellowSec = predYellow,
                    fcSec = predFc,
                    dropSec = predDrop,
                    chargeBt = 200.0,
                    turningBt = 92.0,
                    yellowBt = 150.0,
                    fcBt = 196.0,
                    dropBt = 206.0
                )
            )
        }

        return result
    }

    private fun buildActualBtSeries(
        finalSec: Int,
        actualTurning: Int,
        actualYellow: Int,
        actualFc: Int,
        actualDrop: Int
    ): List<Double> {
        val dropBt = 206.0
        val result = mutableListOf<Double>()

        for (t in 0..finalSec) {
            result.add(
                estimateBt(
                    timeSec = t,
                    turningSec = actualTurning,
                    yellowSec = actualYellow,
                    fcSec = actualFc,
                    dropSec = actualDrop,
                    chargeBt = 200.0,
                    turningBt = 92.0,
                    yellowBt = 150.0,
                    fcBt = 196.0,
                    dropBt = dropBt
                )
            )
        }

        return result
    }

    private fun estimateBt(
        timeSec: Int,
        turningSec: Int,
        yellowSec: Int,
        fcSec: Int,
        dropSec: Int,
        chargeBt: Double,
        turningBt: Double,
        yellowBt: Double,
        fcBt: Double,
        dropBt: Double
    ): Double {
        val t = timeSec.toDouble()
        val turning = turningSec.toDouble()
        val yellow = yellowSec.toDouble()
        val fc = fcSec.toDouble()
        val drop = max(dropSec, fcSec + 30).toDouble()

        return when {
            timeSec <= turningSec -> lerp(t, 0.0, chargeBt, turning, turningBt)
            timeSec <= yellowSec -> lerp(t, turning, turningBt, yellow, yellowBt)
            timeSec <= fcSec -> lerp(t, yellow, yellowBt, fc, fcBt)
            else -> lerp(t, fc, fcBt, drop, dropBt)
        }
    }

    private fun buildControlledRor(btSeries: List<Double>): List<Double> {
        if (btSeries.isEmpty()) return emptyList()

        val raw = MutableList(btSeries.size) { 0.0 }

        for (i in 1 until btSeries.size) {
            val delta = btSeries[i] - btSeries[i - 1]
            raw[i] = delta * 60.0
        }

        val smoothed = MutableList(btSeries.size) { 0.0 }

        for (i in raw.indices) {
            smoothed[i] = weightedAverage(raw, i)
        }

        return enforceReasonableRor(smoothed)
    }

    private fun weightedAverage(values: List<Double>, center: Int): Double {
        val offsets = listOf(-3, -2, -1, 0, 1, 2, 3)
        val weights = listOf(1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0)

        var sum = 0.0
        var weightSum = 0.0

        for (i in offsets.indices) {
            val idx = center + offsets[i]
            if (idx in values.indices) {
                sum += values[idx] * weights[i]
                weightSum += weights[i]
            }
        }

        return if (weightSum > 0.0) sum / weightSum else values[center]
    }

    private fun enforceReasonableRor(values: List<Double>): List<Double> {
        if (values.isEmpty()) return emptyList()

        val out = MutableList(values.size) { 0.0 }
        out[0] = values[0]

        for (i in 1 until values.size) {
            val prev = out[i - 1]
            val current = values[i]
            out[i] = when {
                current > prev + 2.5 -> prev + 2.5
                current < prev - 2.5 -> prev - 2.5
                else -> current
            }
        }

        return out
    }

    private fun buildAnchors(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?
    ): List<CurveAnchor> {
        val anchors = mutableListOf<CurveAnchor>()

        anchors.add(CurveAnchor("Turning", predTurning, false))
        anchors.add(CurveAnchor("Yellow", predYellow, false))
        anchors.add(CurveAnchor("FC", predFc, false))
        anchors.add(CurveAnchor("Drop", predDrop, false))

        actualTurning?.let { anchors.add(CurveAnchor("Turning", it, true)) }
        actualYellow?.let { anchors.add(CurveAnchor("Yellow", it, true)) }
        actualFc?.let { anchors.add(CurveAnchor("FC", it, true)) }
        actualDrop?.let { anchors.add(CurveAnchor("Drop", it, true)) }

        return anchors.sortedBy { it.timeSec }
    }

    private fun buildDeviations(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?
    ): List<CurveDeviation> {
        val result = mutableListOf<CurveDeviation>()

        if (actualTurning != null) {
            result.add(
                CurveDeviation(
                    label = "Turning",
                    deltaSec = actualTurning - predTurning,
                    severity = deviationSeverity(actualTurning - predTurning)
                )
            )
        }

        if (actualYellow != null) {
            result.add(
                CurveDeviation(
                    label = "Yellow",
                    deltaSec = actualYellow - predYellow,
                    severity = deviationSeverity(actualYellow - predYellow)
                )
            )
        }

        if (actualFc != null) {
            result.add(
                CurveDeviation(
                    label = "FC",
                    deltaSec = actualFc - predFc,
                    severity = deviationSeverity(actualFc - predFc)
                )
            )
        }

        if (actualDrop != null) {
            result.add(
                CurveDeviation(
                    label = "Drop",
                    deltaSec = actualDrop - predDrop,
                    severity = deviationSeverity(actualDrop - predDrop)
                )
            )
        }

        return result
    }

    private fun deviationSeverity(deltaSec: Int): String {
        val absDelta = abs(deltaSec)
        return when {
            absDelta >= 20 -> "High"
            absDelta >= 10 -> "Medium"
            absDelta >= 4 -> "Low"
            else -> "Minor"
        }
    }

    private fun buildDeviationSummaryText(deviations: List<CurveDeviation>): String {
        if (deviations.isEmpty()) return "No actual deviations yet"
        return deviations.joinToString("\n") {
            "${it.label} ${formatSigned(it.deltaSec)}s  (${it.severity})"
        }
    }

    private fun detectPhaseAtTime(
        timeSec: Int,
        turningSec: Int,
        yellowSec: Int,
        fcSec: Int,
        dropSec: Int
    ): String {
        return when {
            timeSec >= dropSec -> "Finished"
            timeSec >= fcSec -> "Development"
            timeSec >= yellowSec -> "Maillard / Pre-FC"
            timeSec >= turningSec -> "Drying"
            else -> "Pre-Turning"
        }
    }

    private fun lerp(
        x: Double,
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double
    ): Double {
        if (x1 == x0) return y1
        val ratio = ((x - x0) / (x1 - x0)).coerceIn(0.0, 1.0)
        return y0 + (y1 - y0) * ratio
    }

    private fun formatSigned(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
