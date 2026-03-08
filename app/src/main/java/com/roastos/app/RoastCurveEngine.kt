package com.roastos.app

import kotlin.math.max

data class CurvePoint(
    val timeSec: Int,
    val bt: Double,
    val ror: Double
)

data class CurveAnchor(
    val label: String,
    val timeSec: Int,
    val isActual: Boolean
)

data class RoastCurveResult(
    val points: List<CurvePoint>,
    val anchors: List<CurveAnchor>,
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

        val finalSec = max(
            predDrop,
            max(
                timeline.actual.dropSec ?: 0,
                max(
                    timeline.actual.fcSec ?: 0,
                    max(
                        timeline.actual.yellowSec ?: 0,
                        timeline.actual.turningSec ?: 0
                    )
                )
            )
        ).coerceAtLeast(300)

        val btSeries = mutableListOf<Double>()
        var t = 0
        while (t <= finalSec) {
            btSeries.add(
                estimateBt(
                    timeSec = t,
                    predTurning = predTurning,
                    predYellow = predYellow,
                    predFc = predFc,
                    predDrop = predDrop
                )
            )
            t += 1
        }

        val smoothedRor = buildSmoothedRor(btSeries)

        val points = mutableListOf<CurvePoint>()
        for (i in btSeries.indices) {
            points.add(
                CurvePoint(
                    timeSec = i,
                    bt = btSeries[i],
                    ror = smoothedRor.getOrElse(i) { 0.0 }
                )
            )
        }

        val anchors = buildAnchors(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = timeline.actual.turningSec,
            actualYellow = timeline.actual.yellowSec,
            actualFc = timeline.actual.fcSec,
            actualDrop = timeline.actual.dropSec
        )

        val summary = """
Curve Engine v1.1

Points ${points.size}
Pred Turning ${predTurning}s
Pred Yellow ${predYellow}s
Pred FC ${predFc}s
Pred Drop ${predDrop}s

Actual Turning ${timeline.actual.turningSec?.toString() ?: "-"}
Actual Yellow ${timeline.actual.yellowSec?.toString() ?: "-"}
Actual FC ${timeline.actual.fcSec?.toString() ?: "-"}
Actual Drop ${timeline.actual.dropSec?.toString() ?: "-"}

Resolution 1s
ROR Smoothed Basic
        """.trimIndent()

        return RoastCurveResult(
            points = points,
            anchors = anchors,
            summary = summary
        )
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

    private fun estimateBt(
        timeSec: Int,
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): Double {
        val t = timeSec.toDouble()
        val turning = predTurning.toDouble()
        val yellow = predYellow.toDouble()
        val fc = predFc.toDouble()
        val drop = max(predDrop, predFc + 30).toDouble()

        return when {
            timeSec <= predTurning -> {
                lerp(
                    x = t,
                    x0 = 0.0,
                    y0 = 200.0,
                    x1 = turning,
                    y1 = 92.0
                )
            }

            timeSec <= predYellow -> {
                lerp(
                    x = t,
                    x0 = turning,
                    y0 = 92.0,
                    x1 = yellow,
                    y1 = 150.0
                )
            }

            timeSec <= predFc -> {
                lerp(
                    x = t,
                    x0 = yellow,
                    y0 = 150.0,
                    x1 = fc,
                    y1 = 196.0
                )
            }

            else -> {
                lerp(
                    x = t,
                    x0 = fc,
                    y0 = 196.0,
                    x1 = drop,
                    y1 = 206.0
                )
            }
        }
    }

    private fun buildSmoothedRor(btSeries: List<Double>): List<Double> {
        if (btSeries.isEmpty()) return emptyList()

        val raw = MutableList(btSeries.size) { 0.0 }

        for (i in 1 until btSeries.size) {
            val delta = btSeries[i] - btSeries[i - 1]
            raw[i] = delta * 60.0
        }

        val smooth = MutableList(btSeries.size) { 0.0 }

        for (i in raw.indices) {
            val start = max(0, i - 3)
            val end = minOf(raw.lastIndex, i + 3)

            var sum = 0.0
            var count = 0

            for (j in start..end) {
                sum += raw[j]
                count += 1
            }

            smooth[i] = if (count > 0) sum / count.toDouble() else raw[i]
        }

        return smooth
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
}
