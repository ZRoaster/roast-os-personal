package com.roastos.app

import kotlin.math.abs

data class RoastCurvePredictionV3(
    val bt: Double,
    val ror: Double,
    val predictedTurning: Double?,
    val predictedYellow: Double?,
    val predictedFc: Double?,
    val predictedDrop: Double?,
    val predictedDevelopment: Double?,
    val predictedDtr: Double?,
    val turningDelta: Double?,
    val yellowDelta: Double?,
    val fcDelta: Double?,
    val dropDelta: Double?,
    val chainScore: Int,
    val chainLabel: String,
    val phase: String,
    val confidence: Int,
    val crashRisk: Boolean,
    val flickRisk: Boolean,
    val rorSlope: Double,
    val rorMomentum: Double,
    val summary: String
)

object RoastCurveEngineV3 {

    private data class Sample(
        val timeMillis: Long,
        val bt: Double
    )

    private val history = mutableListOf<Sample>()
    private val rorHistory = mutableListOf<Double>()

    private const val MAX_POINTS = 120

    private const val TURNING_BT = 95.0
    private const val YELLOW_BT = 150.0
    private const val FC_BT = 198.0

    private const val DEV_BASE_SEC = 75.0
    private const val DEV_MIN_SEC = 55.0
    private const val DEV_MAX_SEC = 120.0

    fun reset() {
        history.clear()
        rorHistory.clear()
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

        val smoothedBt = computeSmoothedBt()
        val smoothedRor = computeSmoothedRor()
        val phase = detectPhase(smoothedBt)
        val confidence = estimateConfidence()

        rorHistory.add(smoothedRor)
        if (rorHistory.size > 20) {
            rorHistory.removeAt(0)
        }

        val behavior = RorBehaviorAnalyzer.analyze(rorHistory)

        val predictedTurning = predictAnchorTime(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            targetBt = TURNING_BT
        )

        val predictedYellow = predictAnchorTime(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            targetBt = YELLOW_BT
        )

        val predictedFc = predictAnchorTime(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            targetBt = FC_BT
        )

        val predictedDevelopment = predictDevelopmentSeconds(
            currentBt = smoothedBt,
            currentRor = smoothedRor,
            phase = phase
        )

        val predictedDrop = when {
            predictedFc == null || predictedDevelopment == null -> null
            predictedFc <= 0.0 -> predictedDevelopment
            else -> predictedFc + predictedDevelopment
        }

        val predictedDtr = when {
            predictedDrop == null || predictedDevelopment == null -> null
            predictedDrop <= 0.0 -> null
            else -> (predictedDevelopment / predictedDrop) * 100.0
        }

        val baseline = PlannerBaselineStore.current()

        val turningDelta = computeDelta(
            predicted = predictedTurning,
            baseline = baseline?.turningSec?.toDouble()
        )
        val yellowDelta = computeDelta(
            predicted = predictedYellow,
            baseline = baseline?.yellowSec?.toDouble()
        )
        val fcDelta = computeDelta(
            predicted = predictedFc,
            baseline = baseline?.fcSec?.toDouble()
        )
        val dropDelta = computeDelta(
            predicted = predictedDrop,
            baseline = baseline?.dropSec?.toDouble()
        )

        val chainScore = computeChainScore(
            turningDelta = turningDelta,
            yellowDelta = yellowDelta,
            fcDelta = fcDelta,
            dropDelta = dropDelta,
            confidence = confidence
        )

        val chainLabel = classifyChainScore(chainScore)

        val summary = buildSummary(
            bt = smoothedBt,
            ror = smoothedRor,
            predictedTurning = predictedTurning,
            predictedYellow = predictedYellow,
            predictedFc = predictedFc,
            predictedDrop = predictedDrop,
            predictedDevelopment = predictedDevelopment,
            predictedDtr = predictedDtr,
            turningDelta = turningDelta,
            yellowDelta = yellowDelta,
            fcDelta = fcDelta,
            dropDelta = dropDelta,
            chainScore = chainScore,
            chainLabel = chainLabel,
            phase = phase,
            confidence = confidence,
            behavior = behavior
        )

        return RoastCurvePredictionV3(
            bt = smoothedBt,
            ror = smoothedRor,
            predictedTurning = predictedTurning,
            predictedYellow = predictedYellow,
            predictedFc = predictedFc,
            predictedDrop = predictedDrop,
            predictedDevelopment = predictedDevelopment,
            predictedDtr = predictedDtr,
            turningDelta = turningDelta,
            yellowDelta = yellowDelta,
            fcDelta = fcDelta,
            dropDelta = dropDelta,
            chainScore = chainScore,
            chainLabel = chainLabel,
            phase = phase,
            confidence = confidence,
            crashRisk = behavior.crashRisk,
            flickRisk = behavior.flickRisk,
            rorSlope = behavior.slope,
            rorMomentum = behavior.momentum,
            summary = summary
        )
    }

    fun summary(): String {
        return predict().summary
    }

    private fun computeSmoothedBt(): Double {
        val points = history.takeLast(5)
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
        val points = history.takeLast(6)
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
        return localRors.average()
    }

    private fun predictAnchorTime(
        currentBt: Double,
        currentRor: Double,
        targetBt: Double
    ): Double? {
        if (currentRor <= 0.0) return null
        if (currentBt >= targetBt) return 0.0

        val deltaBt = targetBt - currentBt
        val sec = (deltaBt / currentRor) * 60.0

        return if (sec.isFinite() && sec >= 0.0) sec else null
    }

    private fun predictDevelopmentSeconds(
        currentBt: Double,
        currentRor: Double,
        phase: String
    ): Double? {
        var dev = DEV_BASE_SEC

        when {
            currentRor > 11.0 -> dev -= 10.0
            currentRor > 9.0 -> dev -= 5.0
            currentRor < 5.0 -> dev += 15.0
            currentRor < 6.5 -> dev += 8.0
        }

        when (phase) {
            "Development" -> dev -= 5.0
            "Pre-FC" -> dev += 0.0
            else -> dev += 5.0
        }

        if (currentBt > 196.0) dev -= 5.0
        if (currentBt < 185.0) dev += 5.0

        return dev.coerceIn(DEV_MIN_SEC, DEV_MAX_SEC)
    }

    private fun computeDelta(
        predicted: Double?,
        baseline: Double?
    ): Double? {
        if (predicted == null || baseline == null) return null
        return predicted - baseline
    }

    private fun computeChainScore(
        turningDelta: Double?,
        yellowDelta: Double?,
        fcDelta: Double?,
        dropDelta: Double?,
        confidence: Int
    ): Int {
        var score = 100

        score -= deltaPenalty(turningDelta, 10.0)
        score -= deltaPenalty(yellowDelta, 15.0)
        score -= deltaPenalty(fcDelta, 20.0)
        score -= deltaPenalty(dropDelta, 25.0)

        when {
            confidence < 40 -> score -= 10
            confidence < 60 -> score -= 5
        }

        return score.coerceIn(0, 100)
    }

    private fun deltaPenalty(
        value: Double?,
        limit: Double
    ): Int {
        val safeValue = value ?: return 0
        val absValue = abs(safeValue)

        return when {
            absValue > limit * 3.0 -> 24
            absValue > limit * 2.0 -> 14
            absValue > limit -> 6
            else -> 0
        }
    }

    private fun classifyChainScore(score: Int): String {
        return when {
            score > 85 -> "Tight to Baseline"
            score > 70 -> "Mostly Aligned"
            score > 55 -> "Moderately Offset"
            score > 35 -> "Significant Offset"
            else -> "Far from Baseline"
        }
    }

    private fun detectPhase(bt: Double): String {
        return when {
            bt < 105.0 -> "Charge / Turning"
            bt < 150.0 -> "Drying"
            bt < 185.0 -> "Maillard"
            bt < 198.0 -> "Pre-FC"
            else -> "Development"
        }
    }

    private fun estimateConfidence(): Int {
        val points = history.takeLast(6)
        if (points.size < 3) return 40

        val localRors = mutableListOf<Double>()

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]

            val dtSec = (curr.timeMillis - prev.timeMillis).toDouble() / 1000.0
            if (dtSec <= 0.0) continue

            val deltaBt = curr.bt - prev.bt
            localRors.add((deltaBt / dtSec) * 60.0)
        }

        if (localRors.isEmpty()) return 40

        val avg = localRors.average()
        val meanAbsDeviation = localRors.map { abs(it - avg) }.average()

        return when {
            meanAbsDeviation < 1.0 -> 90
            meanAbsDeviation < 2.0 -> 75
            meanAbsDeviation < 3.0 -> 60
            else -> 45
        }
    }

    private fun buildSummary(
        bt: Double,
        ror: Double,
        predictedTurning: Double?,
        predictedYellow: Double?,
        predictedFc: Double?,
        predictedDrop: Double?,
        predictedDevelopment: Double?,
        predictedDtr: Double?,
        turningDelta: Double?,
        yellowDelta: Double?,
        fcDelta: Double?,
        dropDelta: Double?,
        chainScore: Int,
        chainLabel: String,
        phase: String,
        confidence: Int,
        behavior: RorBehavior
    ): String {
        return """
Curve Prediction V3.6

BT
${"%.1f".format(bt)}℃

ROR
${"%.1f".format(ror)}℃/min

Turning
${formatTime(predictedTurning)}

Yellow
${formatTime(predictedYellow)}

FC
${formatTime(predictedFc)}

Drop
${formatTime(predictedDrop)}

Development
${predictedDevelopment?.let { "%.0f".format(it) + "s" } ?: "-"}

DTR
${predictedDtr?.let { "%.1f".format(it) + "%" } ?: "-"}

Turning Δ
${formatDelta(turningDelta)}

Yellow Δ
${formatDelta(yellowDelta)}

FC Δ
${formatDelta(fcDelta)}

Drop Δ
${formatDelta(dropDelta)}

Chain Score
$chainScore

Chain Status
$chainLabel

Phase
$phase

Confidence
$confidence

ROR Label
${behavior.label}

Crash Risk
${if (behavior.crashRisk) "Yes" else "No"}

Flick Risk
${if (behavior.flickRisk) "Yes" else "No"}

ROR Slope
${"%.2f".format(behavior.slope)}

ROR Momentum
${"%.2f".format(behavior.momentum)}
        """.trimIndent()
    }

    private fun formatTime(value: Double?): String {
        return when {
            value == null -> "-"
            value <= 0.0 -> "Now"
            else -> "%.0fs".format(value)
        }
    }

    private fun formatDelta(value: Double?): String {
        return when {
            value == null -> "-"
            value > 0.0 -> "+%.0fs".format(value)
            else -> "%.0fs".format(value)
        }
    }

    private fun emptyPrediction(reason: String): RoastCurvePredictionV3 {
        return RoastCurvePredictionV3(
            bt = 0.0,
            ror = 0.0,
            predictedTurning = null,
            predictedYellow = null,
            predictedFc = null,
            predictedDrop = null,
            predictedDevelopment = null,
            predictedDtr = null,
            turningDelta = null,
            yellowDelta = null,
            fcDelta = null,
            dropDelta = null,
            chainScore = 0,
            chainLabel = "Unknown",
            phase = "Unknown",
            confidence = 0,
            crashRisk = false,
            flickRisk = false,
            rorSlope = 0.0,
            rorMomentum = 0.0,
            summary = reason
        )
    }
}
