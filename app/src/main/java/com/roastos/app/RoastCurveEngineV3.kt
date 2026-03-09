package com.roastos.app

import kotlin.math.abs
import kotlin.math.max

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
    val summary: String
)

object RoastCurveEngineV3 {

    private data class Sample(
        val t: Long,
        val bt: Double
    )

    private val history = mutableListOf<Sample>()

    private const val MAX_POINTS = 120

    private const val TURNING_BT = 95.0
    private const val YELLOW_BT = 150.0
    private const val FC_BT = 198.0

    private const val DEV_BASE = 75.0
    private const val DEV_MIN = 55.0
    private const val DEV_MAX = 120.0

    fun reset() {
        history.clear()
    }

    fun record(bt: Double, timeMillis: Long) {
        history.add(Sample(timeMillis, bt))
        if (history.size > MAX_POINTS) history.removeAt(0)
    }

    fun predict(): RoastCurvePredictionV3 {

        if (history.size < 3) {
            return emptyPrediction("Not enough data")
        }

        val bt = smoothedBt()
        val ror = smoothedRor()

        val phase = detectPhase(bt)
        val confidence = estimateConfidence()

        val turning = predictTime(bt, ror, TURNING_BT)
        val yellow = predictTime(bt, ror, YELLOW_BT)
        val fc = predictTime(bt, ror, FC_BT)

        val dev = predictDevelopment(bt, ror)
        val drop = if (fc != null && dev != null) fc + dev else null

        val dtr =
            if (drop != null && dev != null && drop > 0)
                dev / drop * 100
            else null

        val baseline = PlannerBaselineStore.current()

        val turningDelta = delta(turning, baseline?.turningSec?.toDouble())
        val yellowDelta = delta(yellow, baseline?.yellowSec?.toDouble())
        val fcDelta = delta(fc, baseline?.fcSec?.toDouble())
        val dropDelta = delta(drop, baseline?.dropSec?.toDouble())

        val score = chainScore(
            turningDelta,
            yellowDelta,
            fcDelta,
            dropDelta,
            confidence
        )

        val label = classifyScore(score)

        val summary = buildSummary(
            bt,
            ror,
            turning,
            yellow,
            fc,
            drop,
            dev,
            dtr,
            turningDelta,
            yellowDelta,
            fcDelta,
            dropDelta,
            score,
            label,
            phase,
            confidence
        )

        return RoastCurvePredictionV3(
            bt,
            ror,
            turning,
            yellow,
            fc,
            drop,
            dev,
            dtr,
            turningDelta,
            yellowDelta,
            fcDelta,
            dropDelta,
            score,
            label,
            phase,
            confidence,
            summary
        )
    }

    fun summary(): String {
        return predict().summary
    }

    private fun smoothedBt(): Double {
        val pts = history.takeLast(5)
        var sum = 0.0
        var w = 0.0

        pts.forEachIndexed { i, p ->
            val weight = (i + 1).toDouble()
            sum += p.bt * weight
            w += weight
        }

        return sum / w
    }

    private fun smoothedRor(): Double {

        val pts = history.takeLast(6)
        if (pts.size < 2) return 0.0

        val rors = mutableListOf<Double>()

        for (i in 1 until pts.size) {

            val dt = (pts[i].t - pts[i - 1].t) / 1000.0
            if (dt <= 0) continue

            val db = pts[i].bt - pts[i - 1].bt
            rors.add(db / dt * 60)
        }

        if (rors.isEmpty()) return 0.0

        return rors.average()
    }

    private fun predictTime(bt: Double, ror: Double, target: Double): Double? {

        if (ror <= 0) return null
        if (bt >= target) return 0.0

        val d = target - bt
        val sec = d / ror * 60

        return if (sec.isFinite()) sec else null
    }

    private fun predictDevelopment(bt: Double, ror: Double): Double {

        var dev = DEV_BASE

        when {
            ror > 11 -> dev -= 10
            ror > 9 -> dev -= 5
            ror < 5 -> dev += 15
            ror < 6.5 -> dev += 8
        }

        if (bt > 196) dev -= 5
        if (bt < 185) dev += 5

        return dev.coerceIn(DEV_MIN, DEV_MAX)
    }

    private fun delta(pred: Double?, base: Double?): Double? {
        if (pred == null || base == null) return null
        return pred - base
    }

    private fun chainScore(
        t: Double?,
        y: Double?,
        f: Double?,
        d: Double?,
        conf: Int
    ): Int {

        var s = 100

        s -= penalty(t, 10.0)
        s -= penalty(y, 15.0)
        s -= penalty(f, 20.0)
        s -= penalty(d, 25.0)

        if (conf < 40) s -= 10
        else if (conf < 60) s -= 5

        return s.coerceIn(0, 100)
    }

    private fun penalty(v: Double?, limit: Double): Int {

        val value = abs(v ?: return 0.0)

        return when {
            value > limit * 3 -> 24
            value > limit * 2 -> 14
            value > limit -> 6
            else -> 0
        }
    }

    private fun classifyScore(score: Int): String {

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
            bt < 105 -> "Charge / Turning"
            bt < 150 -> "Drying"
            bt < 185 -> "Maillard"
            bt < 198 -> "Pre-FC"
            else -> "Development"
        }
    }

    private fun estimateConfidence(): Int {

        val pts = history.takeLast(6)

        if (pts.size < 3) return 40

        val r = mutableListOf<Double>()

        for (i in 1 until pts.size) {

            val dt = (pts[i].t - pts[i - 1].t) / 1000.0
            if (dt <= 0) continue

            val db = pts[i].bt - pts[i - 1].bt
            r.add(db / dt * 60)
        }

        if (r.isEmpty()) return 40

        val avg = r.average()
        val varr = r.map { abs(it - avg) }.average()

        return when {
            varr < 1 -> 90
            varr < 2 -> 75
            varr < 3 -> 60
            else -> 45
        }
    }

    private fun buildSummary(
        bt: Double,
        ror: Double,
        turning: Double?,
        yellow: Double?,
        fc: Double?,
        drop: Double?,
        dev: Double?,
        dtr: Double?,
        td: Double?,
        yd: Double?,
        fd: Double?,
        dd: Double?,
        score: Int,
        label: String,
        phase: String,
        confidence: Int
    ): String {

        return """
Curve Prediction V3.5

BT
${"%.1f".format(bt)}℃

ROR
${"%.1f".format(ror)}℃/min

Turning
${time(turning)}

Yellow
${time(yellow)}

FC
${time(fc)}

Drop
${time(drop)}

Development
${dev?.let { "%.0f".format(it) + "s" } ?: "-"}

DTR
${dtr?.let { "%.1f".format(it) + "%" } ?: "-"}

Turning Δ
${deltaText(td)}

Yellow Δ
${deltaText(yd)}

FC Δ
${deltaText(fd)}

Drop Δ
${deltaText(dd)}

Chain Score
$score

Chain Status
$label

Phase
$phase

Confidence
$confidence
""".trimIndent()
    }

    private fun time(v: Double?): String {
        if (v == null) return "-"
        if (v <= 0) return "Now"
        return "%.0fs".format(v)
    }

    private fun deltaText(v: Double?): String {
        if (v == null) return "-"
        return if (v > 0) "+%.0fs".format(v) else "%.0fs".format(v)
    }

    private fun emptyPrediction(reason: String): RoastCurvePredictionV3 {

        return RoastCurvePredictionV3(
            0.0,
            0.0,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            "Unknown",
            "Unknown",
            0,
            reason
        )
    }
}
