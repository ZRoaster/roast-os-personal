package com.roastos.app

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class RoastCurvePredictionV3(
    val smoothedBt: Double,
    val smoothedRor: Double,
    val predictedBt15: Double,
    val predictedBt30: Double,
    val predictedBt45: Double,
    val predictedFcTimeSec: Double?,
    val phase: String,
    val trend: String,
    val confidence: Int,
    val summary: String
)

object RoastCurveEngineV3 {

    private data class Sample(
        val timeMillis: Long,
        val bt: Double
    )

    private val history = mutableListOf<Sample>()

    private const val MAX_POINTS = 120
    private const val FC_TARGET_BT = 198.0

    // FC 稳定化参数
    private const val FC_ACTIVE_BT_MIN = 170.0
    private const val FC_MIN_ROR = 3.5
    private const val FC_SMOOTHING_ALPHA = 0.22
    private const val FC_MAX_STEP_SEC = 12.0

    private var fcPredictionCacheSec: Double? = null

    fun reset() {
        history.clear()
        fcPredictionCacheSec = null
    }

    fun record(
        bt: Double,
        timeMillis: Long
    ) {
        history.add(
            Sample(
                timeMillis = timeMillis,
                bt = bt
            )
        )

        if (history.size > MAX_POINTS) {
            history.removeAt(0)
        }
    }

    fun predict(): RoastCurvePredictionV3 {
        if (history.size < 3) {
            return emptyPrediction("Not enough data")
        }

        val smoothedBt = weightedAverageBt(lastPoints(5))
        val smoothedRor = computeSmoothedRor()

        val predictedBt15 = smoothedBt + smoothedRor * (15.0 / 60.0)
        val predictedBt30 = smoothedBt + smoothedRor * (30.0 / 60.0)
        val predictedBt45 = smoothedBt + smoothedRor * (45.0 / 60.0)

        val phase = detectPhase(smoothedBt)
        val confidence = estimateConfidence()

        val rawFcTimeSec = predictRawFcTimeSec(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            confidence = confidence
        )

        val stabilizedFcTimeSec = stabilizeFcPrediction(rawFcTimeSec)

        val trend = detectTrend(
            smoothedRor = smoothedRor,
            predictedFcTimeSec = stabilizedFcTimeSec
        )

        val summary = buildSummary(
            smoothedBt = smoothedBt,
            smoothedRor = smoothedRor,
            predictedBt15 = predictedBt15,
            predictedBt30 = predictedBt30,
            predictedBt45 = predictedBt45,
            predictedFcTimeSec = stabilizedFcTimeSec,
            phase = phase,
            trend = trend,
            confidence = confidence
        )

        return RoastCurvePredictionV3(
            smoothedBt = smoothedBt,
            smoothedRor = smoothedRor,
            predictedBt15 = predictedBt15,
            predictedBt30 = predictedBt30,
            predictedBt45 = predictedBt45,
            predictedFcTimeSec = stabilizedFcTimeSec,
            phase = phase,
            trend = trend,
            confidence = confidence,
            summary = summary
        )
    }

    fun summary(): String {
        return predict().summary
    }

    private fun emptyPrediction(reason: String): RoastCurvePredictionV3 {
        val summary = """
Curve Prediction V3.1

Status
$reason

Smoothed BT
0.0 ℃

Smoothed ROR
0.0 ℃/min

BT +15s
0.0 ℃

BT +30s
0.0 ℃

BT +45s
0.0 ℃

Predicted FC
-

Phase
Unknown

Trend
Unknown

Confidence
0
        """.trimIndent()

        return RoastCurvePredictionV3(
            smoothedBt = 0.0,
            smoothedRor = 0.0,
            predictedBt15 = 0.0,
            predictedBt30 = 0.0,
            predictedBt45 = 0.0,
            predictedFcTimeSec = null,
            phase = "Unknown",
            trend = "Unknown",
            confidence = 0,
            summary = summary
        )
    }

    private fun lastPoints(count: Int): List<Sample> {
        return history.takeLast(count)
    }

    private fun weightedAverageBt(points: List<Sample>): Double {
        if (points.isEmpty()) return 0.0

        var weightedSum = 0.0
        var totalWeight = 0.0

        points.forEachIndexed { index, sample ->
            val weight = (index + 1).toDouble()
            weightedSum += sample.bt * weight
            totalWeight += weight
        }

        return if (totalWeight > 0.0) weightedSum / totalWeight else 0.0
    }

    private fun computeSmoothedRor(): Double {
        val points = history.takeLast(8)
        if (points.size < 2) return 0.0

        val localRors = mutableListOf<Double>()

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]

            val dtSec = (curr.timeMillis - prev.timeMillis).toDouble() / 1000.0
            if (dtSec <= 0.0) continue

            val deltaBt = curr.bt - prev.bt
            val ror = (deltaBt / dtSec) * 60.0
            localRors.add(ror)
        }

        if (localRors.isEmpty()) return 0.0

        var weightedSum = 0.0
        var totalWeight = 0.0

        localRors.forEachIndexed { index, value ->
            val weight = (index + 1).toDouble()
            weightedSum += value * weight
            totalWeight += weight
        }

        return if (totalWeight > 0.0) weightedSum / totalWeight else 0.0
    }

    private fun predictRawFcTimeSec(
        currentBt: Double,
        currentRor: Double,
        confidence: Int
    ): Double? {
        if (currentBt >= FC_TARGET_BT) return 0.0
        if (currentBt < FC_ACTIVE_BT_MIN) return null
        if (currentRor <= FC_MIN_ROR) return null
        if (confidence < 35) return null

        val deltaBt = FC_TARGET_BT - currentBt
        val rawSec = (deltaBt / currentRor) * 60.0

        return if (rawSec.isFinite() && rawSec >= 0.0) rawSec else null
    }

    private fun stabilizeFcPrediction(rawFcTimeSec: Double?): Double? {
        if (rawFcTimeSec == null) {
            // 不立刻清空缓存，避免显示剧烈闪断；但缓存过久会自然被新数据覆盖
            return fcPredictionCacheSec
        }

        val cached = fcPredictionCacheSec
        if (cached == null) {
            fcPredictionCacheSec = rawFcTimeSec
            return rawFcTimeSec
        }

        val delta = rawFcTimeSec - cached
        val limitedTarget = cached + delta.coerceIn(-FC_MAX_STEP_SEC, FC_MAX_STEP_SEC)
        val smoothed = cached + (limitedTarget - cached) * FC_SMOOTHING_ALPHA

        fcPredictionCacheSec = max(0.0, smoothed)
        return fcPredictionCacheSec
    }

    private fun detectPhase(smoothedBt: Double): String {
        return when {
            smoothedBt < 105.0 -> "Charge / Turning"
            smoothedBt < 150.0 -> "Drying"
            smoothedBt < 185.0 -> "Maillard"
            smoothedBt < 198.0 -> "Pre-FC"
            else -> "Development"
        }
    }

    private fun detectTrend(
        smoothedRor: Double,
        predictedFcTimeSec: Double?
    ): String {
        return when {
            smoothedRor < 3.0 -> "Crash Risk"
            smoothedRor < 5.5 -> "Low Momentum"
            smoothedRor > 16.0 -> "Runaway Heat"
            smoothedRor > 12.0 -> "Too Fast"
            predictedFcTimeSec != null && predictedFcTimeSec < 15.0 -> "FC Imminent"
            predictedFcTimeSec != null && predictedFcTimeSec < 35.0 -> "Approaching FC"
            else -> "Stable"
        }
    }

    private fun estimateConfidence(): Int {
        val points = history.takeLast(8)
        if (points.size < 4) return 35

        val localRors = mutableListOf<Double>()

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val dtSec = (curr.timeMillis - prev.timeMillis).toDouble() / 1000.0
            if (dtSec <= 0.0) continue

            val deltaBt = curr.bt - prev.bt
            localRors.add((deltaBt / dtSec) * 60.0)
        }

        if (localRors.isEmpty()) return 30

        val avg = localRors.average()
        val variance = localRors.map { abs(it - avg) }.average()

        return when {
            variance < 0.8 -> 90
            variance < 1.5 -> 78
            variance < 2.5 -> 65
            variance < 4.0 -> 50
            else -> 35
        }
    }

    private fun buildSummary(
        smoothedBt: Double,
        smoothedRor: Double,
        predictedBt15: Double,
        predictedBt30: Double,
        predictedBt45: Double,
        predictedFcTimeSec: Double?,
        phase: String,
        trend: String,
        confidence: Int
    ): String {
        val fcText = when {
            predictedFcTimeSec == null -> "-"
            predictedFcTimeSec <= 0.0 -> "Now"
            else -> "%.0f".format(predictedFcTimeSec) + "s"
        }

        return """
Curve Prediction V3.1

Smoothed BT
${"%.1f".format(smoothedBt)} ℃

Smoothed ROR
${"%.1f".format(smoothedRor)} ℃/min

BT +15s
${"%.1f".format(predictedBt15)} ℃

BT +30s
${"%.1f".format(predictedBt30)} ℃

BT +45s
${"%.1f".format(predictedBt45)} ℃

Predicted FC
$fcText

Phase
$phase

Trend
$trend

Confidence
$confidence
        """.trimIndent()
    }
}
