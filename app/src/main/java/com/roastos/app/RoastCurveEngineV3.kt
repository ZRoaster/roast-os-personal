package com.roastos.app

import kotlin.math.abs
import kotlin.math.max

data class RoastCurvePredictionV3(
    val smoothedBt: Double,
    val smoothedRor: Double,
    val predictedBt15: Double,
    val predictedBt30: Double,
    val predictedBt45: Double,
    val predictedTurningTimeSec: Double?,
    val predictedYellowTimeSec: Double?,
    val predictedFcTimeSec: Double?,
    val predictedDropTimeSec: Double?,
    val predictedDevelopmentSec: Double?,
    val predictedDtrPercent: Double?,
    val baselineTurningDeltaSec: Double?,
    val baselineYellowDeltaSec: Double?,
    val baselineFcDeltaSec: Double?,
    val baselineDropDeltaSec: Double?,
    val chainTrendScore: Int,
    val chainTrendLabel: String,
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

    private data class ConstrainedAnchors(
        val turningSec: Double?,
        val yellowSec: Double?,
        val fcSec: Double?,
        val developmentSec: Double?
    )

    private val history = mutableListOf<Sample>()

    private const val MAX_POINTS = 120

    private const val TURNING_TARGET_BT = 95.0
    private const val YELLOW_TARGET_BT = 150.0
    private const val FC_TARGET_BT = 198.0

    private const val TURNING_ACTIVE_BT_MAX = 120.0
    private const val TURNING_MIN_ROR = 1.2
    private const val TURNING_SMOOTHING_ALPHA = 0.18
    private const val TURNING_MAX_STEP_SEC = 10.0

    private const val YELLOW_ACTIVE_BT_MIN = 120.0
    private const val YELLOW_MIN_ROR = 3.0
    private const val YELLOW_SMOOTHING_ALPHA = 0.20
    private const val YELLOW_MAX_STEP_SEC = 14.0

    private const val FC_ACTIVE_BT_MIN = 170.0
    private const val FC_MIN_ROR = 3.5
    private const val FC_SMOOTHING_ALPHA = 0.22
    private const val FC_MAX_STEP_SEC = 12.0

    private const val DEV_BASE_SEC = 75.0
    private const val DEV_MIN_SEC = 55.0
    private const val DEV_MAX_SEC = 120.0
    private const val DEV_SMOOTHING_ALPHA = 0.25
    private const val DEV_MAX_STEP_SEC = 10.0

    private const val MIN_TURNING_TO_YELLOW_SEC = 80.0
    private const val MAX_TURNING_TO_YELLOW_SEC = 260.0
    private const val MIN_YELLOW_TO_FC_SEC = 90.0
    private const val MAX_YELLOW_TO_FC_SEC = 330.0
    private const val MIN_FC_TO_DROP_SEC = 45.0
    private const val MAX_FC_TO_DROP_SEC = 140.0

    private var turningPredictionCacheSec: Double? = null
    private var yellowPredictionCacheSec: Double? = null
    private var fcPredictionCacheSec: Double? = null
    private var devPredictionCacheSec: Double? = null

    fun reset() {
        history.clear()
        turningPredictionCacheSec = null
        yellowPredictionCacheSec = null
        fcPredictionCacheSec = null
        devPredictionCacheSec = null
    }

    fun record(
        bt: Double,
        timeMillis: Long
    ) {
        history.add(Sample(timeMillis = timeMillis, bt = bt))

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
        val phase = detectPhase(smoothedBt)
        val confidence = estimateConfidence()

        val predictedBt15 = smoothedBt + smoothedRor * (15.0 / 60.0)
        val predictedBt30 = smoothedBt + smoothedRor * (30.0 / 60.0)
        val predictedBt45 = smoothedBt + smoothedRor * (45.0 / 60.0)

        val rawTurningTimeSec = predictRawTurningTimeSec(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            confidence = confidence
        )
        val stabilizedTurningTimeSec = stabilizeTurningPrediction(rawTurningTimeSec)

        val rawYellowTimeSec = predictRawYellowTimeSec(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            confidence = confidence
        )
        val stabilizedYellowTimeSec = stabilizeYellowPrediction(rawYellowTimeSec)

        val rawFcTimeSec = predictRawFcTimeSec(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            confidence = confidence
        )
        val stabilizedFcTimeSec = stabilizeFcPrediction(rawFcTimeSec)

        val rawDevelopmentSec = predictRawDevelopmentSec(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            phase = phase,
            confidence = confidence
        )
        val stabilizedDevelopmentSec = stabilizeDevelopmentPrediction(rawDevelopmentSec)

        val constrainedAnchors = applyAnchorConsistency(
            turningSec = stabilizedTurningTimeSec,
            yellowSec = stabilizedYellowTimeSec,
            fcSec = stabilizedFcTimeSec,
            developmentSec = stabilizedDevelopmentSec
        )

        val predictedDropTimeSec = when {
            constrainedAnchors.fcSec == null || constrainedAnchors.developmentSec == null -> null
            constrainedAnchors.fcSec <= 0.0 -> constrainedAnchors.developmentSec
            else -> constrainedAnchors.fcSec + constrainedAnchors.developmentSec
        }

        val predictedDtrPercent = when {
            predictedDropTimeSec == null || constrainedAnchors.developmentSec == null -> null
            predictedDropTimeSec <= 0.0 -> null
            else -> (constrainedAnchors.developmentSec / predictedDropTimeSec) * 100.0
        }

        val baseline = PlannerBaselineStore.current()

        val baselineTurningDeltaSec = computeDeltaSec(
            predicted = constrainedAnchors.turningSec,
            baseline = baseline?.turningSec?.toDouble()
        )

        val baselineYellowDeltaSec = computeDeltaSec(
            predicted = constrainedAnchors.yellowSec,
            baseline = baseline?.yellowSec?.toDouble()
        )

        val baselineFcDeltaSec = computeDeltaSec(
            predicted = constrainedAnchors.fcSec,
            baseline = baseline?.fcSec?.toDouble()
        )

        val baselineDropDeltaSec = computeDeltaSec(
            predicted = predictedDropTimeSec,
            baseline = baseline?.dropSec?.toDouble()
        )

        val chainTrendScore = computeChainTrendScore(
            baselineTurningDeltaSec = baselineTurningDeltaSec,
            baselineYellowDeltaSec = baselineYellowDeltaSec,
            baselineFcDeltaSec = baselineFcDeltaSec,
            baselineDropDeltaSec = baselineDropDeltaSec,
            confidence = confidence
        )

        val chainTrendLabel = classifyChainTrendScore(chainTrendScore)

        val trend = detectTrend(
            smoothedRor = smoothedRor,
            predictedTurningTimeSec = constrainedAnchors.turningSec,
            predictedYellowTimeSec = constrainedAnchors.yellowSec,
            predictedFcTimeSec = constrainedAnchors.fcSec,
            predictedDropTimeSec = predictedDropTimeSec,
            baselineTurningDeltaSec = baselineTurningDeltaSec,
            baselineYellowDeltaSec = baselineYellowDeltaSec,
            baselineFcDeltaSec = baselineFcDeltaSec,
            baselineDropDeltaSec = baselineDropDeltaSec,
            chainTrendLabel = chainTrendLabel
        )

        val summary = buildSummary(
            smoothedBt = smoothedBt,
            smoothedRor = smoothedRor,
            predictedBt15 = predictedBt15,
            predictedBt30 = predictedBt30,
            predictedBt45 = predictedBt45,
            predictedTurningTimeSec = constrainedAnchors.turningSec,
            predictedYellowTimeSec = constrainedAnchors.yellowSec,
            predictedFcTimeSec = constrainedAnchors.fcSec,
            predictedDropTimeSec = predictedDropTimeSec,
            predictedDevelopmentSec = constrainedAnchors.developmentSec,
            predictedDtrPercent = predictedDtrPercent,
            baselineTurningDeltaSec = baselineTurningDeltaSec,
            baselineYellowDeltaSec = baselineYellowDeltaSec,
            baselineFcDeltaSec = baselineFcDeltaSec,
            baselineDropDeltaSec = baselineDropDeltaSec,
            chainTrendScore = chainTrendScore,
            chainTrendLabel = chainTrendLabel,
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
            predictedTurningTimeSec = constrainedAnchors.turningSec,
            predictedYellowTimeSec = constrainedAnchors.yellowSec,
            predictedFcTimeSec = constrainedAnchors.fcSec,
            predictedDropTimeSec = predictedDropTimeSec,
            predictedDevelopmentSec = constrainedAnchors.developmentSec,
            predictedDtrPercent = predictedDtrPercent,
            baselineTurningDeltaSec = baselineTurningDeltaSec,
            baselineYellowDeltaSec = baselineYellowDeltaSec,
            baselineFcDeltaSec = baselineFcDeltaSec,
            baselineDropDeltaSec = baselineDropDeltaSec,
            chainTrendScore = chainTrendScore,
            chainTrendLabel = chainTrendLabel,
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
Curve Prediction V3.5

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

Predicted Turning
-

Predicted Yellow
-

Predicted FC
-

Predicted Drop
-

Predicted Development
-

Predicted DTR
-

Baseline Turning Δ
-

Baseline Yellow Δ
-

Baseline FC Δ
-

Baseline Drop Δ
-

Chain Trend Score
0

Chain Trend Label
Unknown

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
            predictedTurningTimeSec = null,
            predictedYellowTimeSec = null,
            predictedFcTimeSec = null,
            predictedDropTimeSec = null,
            predictedDevelopmentSec = null,
            predictedDtrPercent = null,
            baselineTurningDeltaSec = null,
            baselineYellowDeltaSec = null,
            baselineFcDeltaSec = null,
            baselineDropDeltaSec = null,
            chainTrendScore = 0,
            chainTrendLabel = "Unknown",
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
            localRors.add((deltaBt / dtSec) * 60.0)
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

    private fun predictRawTurningTimeSec(
        currentBt: Double,
        currentRor: Double,
        confidence: Int
    ): Double? {
        if (currentBt >= TURNING_TARGET_BT) return 0.0
        if (currentBt > TURNING_ACTIVE_BT_MAX) return null
        if (currentRor <= TURNING_MIN_ROR) return null
        if (confidence < 25) return null

        val deltaBt = TURNING_TARGET_BT - currentBt
        val rawSec = (deltaBt / currentRor) * 60.0
        return if (rawSec.isFinite() && rawSec >= 0.0) rawSec else null
    }

    private fun stabilizeTurningPrediction(rawTurningSec: Double?): Double? {
        if (rawTurningSec == null) return turningPredictionCacheSec

        val cached = turningPredictionCacheSec
        if (cached == null) {
            turningPredictionCacheSec = rawTurningSec
            return rawTurningSec
        }

        val delta = rawTurningSec - cached
        val limitedTarget = cached + delta.coerceIn(-TURNING_MAX_STEP_SEC, TURNING_MAX_STEP_SEC)
        val smoothed = cached + (limitedTarget - cached) * TURNING_SMOOTHING_ALPHA

        turningPredictionCacheSec = max(0.0, smoothed)
        return turningPredictionCacheSec
    }

    private fun predictRawYellowTimeSec(
        currentBt: Double,
        currentRor: Double,
        confidence: Int
    ): Double? {
        if (currentBt >= YELLOW_TARGET_BT) return 0.0
        if (currentBt < YELLOW_ACTIVE_BT_MIN) return null
        if (currentRor <= YELLOW_MIN_ROR) return null
        if (confidence < 30) return null

        val deltaBt = YELLOW_TARGET_BT - currentBt
        val rawSec = (deltaBt / currentRor) * 60.0
        return if (rawSec.isFinite() && rawSec >= 0.0) rawSec else null
    }

    private fun stabilizeYellowPrediction(rawYellowSec: Double?): Double? {
        if (rawYellowSec == null) return yellowPredictionCacheSec

        val cached = yellowPredictionCacheSec
        if (cached == null) {
            yellowPredictionCacheSec = rawYellowSec
            return rawYellowSec
        }

        val delta = rawYellowSec - cached
        val limitedTarget = cached + delta.coerceIn(-YELLOW_MAX_STEP_SEC, YELLOW_MAX_STEP_SEC)
        val smoothed = cached + (limitedTarget - cached) * YELLOW_SMOOTHING_ALPHA

        yellowPredictionCacheSec = max(0.0, smoothed)
        return yellowPredictionCacheSec
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
        if (rawFcTimeSec == null) return fcPredictionCacheSec

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

    private fun predictRawDevelopmentSec(
        currentBt: Double,
        currentRor: Double,
        phase: String,
        confidence: Int
    ): Double? {
        if (confidence < 30) return null
        if (currentBt < 175.0 && phase != "Pre-FC" && phase != "Development") return null

        var dev = DEV_BASE_SEC

        when {
            currentRor >= 11.0 -> dev -= 10.0
            currentRor >= 9.0 -> dev -= 5.0
            currentRor <= 5.0 -> dev += 15.0
            currentRor <= 6.5 -> dev += 8.0
        }

        when (phase) {
            "Development" -> dev -= 5.0
            "Pre-FC" -> dev += 0.0
            else -> dev += 5.0
        }

        if (currentBt >= 196.0) dev -= 5.0
        if (currentBt <= 185.0) dev += 5.0

        return dev.coerceIn(DEV_MIN_SEC, DEV_MAX_SEC)
    }

    private fun stabilizeDevelopmentPrediction(rawDevSec: Double?): Double? {
        if (rawDevSec == null) return devPredictionCacheSec

        val cached = devPredictionCacheSec
        if (cached == null) {
            devPredictionCacheSec = rawDevSec
            return rawDevSec
        }

        val delta = rawDevSec - cached
        val limitedTarget = cached + delta.coerceIn(-DEV_MAX_STEP_SEC, DEV_MAX_STEP_SEC)
        val smoothed = cached + (limitedTarget - cached) * DEV_SMOOTHING_ALPHA

        devPredictionCacheSec = smoothed.coerceIn(DEV_MIN_SEC, DEV_MAX_SEC)
        return devPredictionCacheSec
    }

    private fun applyAnchorConsistency(
        turningSec: Double?,
        yellowSec: Double?,
        fcSec: Double?,
        developmentSec: Double?
    ): ConstrainedAnchors {
        var t = turningSec
        var y = yellowSec
        var fc = fcSec
        var dev = developmentSec

        if (t != null && y != null) {
            val ty = y - t
            when {
                ty < MIN_TURNING_TO_YELLOW_SEC -> y = t + MIN_TURNING_TO_YELLOW_SEC
                ty > MAX_TURNING_TO_YELLOW_SEC -> y = t + MAX_TURNING_TO_YELLOW_SEC
            }
        }

        if (y != null && fc != null) {
            val yf = fc - y
            when {
                yf < MIN_YELLOW_TO_FC_SEC -> fc = y + MIN_YELLOW_TO_FC_SEC
                yf > MAX_YELLOW_TO_FC_SEC -> fc = y + MAX_YELLOW_TO_FC_SEC
            }
        }

        if (fc != null && dev != null) {
            dev = dev.coerceIn(MIN_FC_TO_DROP_SEC, MAX_FC_TO_DROP_SEC)
        }

        if (t != null && y == null && t < 20.0) {
            y = t + MIN_TURNING_TO_YELLOW_SEC
        }

        if (y != null && fc == null && dev != null && y < 25.0) {
            dev = max(dev, 65.0)
        }

        return ConstrainedAnchors(
            turningSec = t,
            yellowSec = y,
            fcSec = fc,
            developmentSec = dev
        )
    }

    private fun computeDeltaSec(
        predicted: Double?,
        baseline: Double?
    ): Double? {
        if (predicted == null || baseline == null) return null
        return predicted - baseline
    }

    private fun computeChainTrendScore(
        baselineTurningDeltaSec: Double?,
        baselineYellowDeltaSec: Double?,
        baselineFcDeltaSec: Double?,
        baselineDropDeltaSec: Double?,
        confidence: Int
    ): Int {
        var score = 100

        score -= deltaPenalty(baselineTurningDeltaSec, mild = 8.0, mid = 15.0, high = 25.0)
        score -= deltaPenalty(baselineYellowDeltaSec, mild = 12.0, mid = 20.0, high = 35.0)
        score -= deltaPenalty(baselineFcDeltaSec, mild = 15.0, mid = 25.0, high = 40.0)
        score -= deltaPenalty(baselineDropDeltaSec, mild = 18.0, mid = 30.0, high = 50.0)

        when {
            confidence < 40 -> score -= 12
            confidence < 55 -> score -= 8
            confidence < 70 -> score -= 4
        }

        return score.coerceIn(0, 100)
    }

    private fun deltaPenalty(
        value: Double?,
        mild: Double,
        mid: Double,
        high: Double
    ): Int {
        val absValue = abs(value ?: return 0)
        return when {
            absValue >= high -> 24
            absValue >= mid -> 14
            absValue >= mild -> 6
            else -> 0
        }
    }

    private fun classifyChainTrendScore(score: Int): String {
        return when {
       
