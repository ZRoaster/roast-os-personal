package com.roastos.app

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RoastEngine {

    private fun clamp(v: Double, lo: Double, hi: Double): Double {
        return max(lo, min(hi, v))
    }

    fun parseMMSS(str: String): Int? {
        val parts = str.trim().split(":")
        if (parts.size != 2) return null
        val m = parts[0].toIntOrNull() ?: return null
        val s = parts[1].toIntOrNull() ?: return null
        return m * 60 + s
    }

    fun toMMSS(sec: Double): String {
        val total = sec.roundToInt().coerceAtLeast(0)
        val m = total / 60
        val s = total % 60
        return "%d:%02d".format(m, s)
    }

    private fun processLabel(process: String): String {
        return when (process) {
            "washed" -> "水洗"
            "honey_washed" -> "蜜处理"
            "natural" -> "普通日晒"
            "honey_natural" -> "花果日晒"
            "anaerobic" -> "厌氧"
            "fermented" -> "特殊发酵"
            else -> process
        }
    }

    fun calcCard(input: PlannerInput): PlannerResult {
        val dens = input.density
        val moist = input.moisture
        val batch = input.batchNum
        val temp = input.envTemp
        val rh = input.envRH
        val m = input.learnM
        val K = input.learnK
        val w = input.learnW
        val ttSec = input.ttSec.toDouble()
        val tySec = input.tySec?.toDouble()

        var chargeBT = when (batch) {
            1 -> 205
            2 -> 204
            3 -> 203
            else -> 202
        }

        chargeBT -= ( ((temp - 22.0) / 5.0).roundToInt() * 2 )
        if (rh >= 55) chargeBT -= 1
        if (rh <= 25) chargeBT += 1

        if (dens > 860 && moist > 11.0) chargeBT += 1
        else if (dens < 820 || moist < 10.0) chargeBT -= 1

        var rpm = 7
        if (dens > 860 && input.beanSize == "large") rpm = 8
        if (dens < 820 || input.beanSize == "small") rpm = 6

        val preheatPa = when (input.orientation) {
            "clean" -> 16
            "stable" -> 14
            "thick" -> 12
            else -> 14
        }

        val fc1 = m * ttSec + K
        val fc2 = tySec?.let { 1.6 * it }

        var fcPredSec = if (fc2 != null) {
            w * fc1 + (1.0 - w) * fc2
        } else {
            fc1
        }
        fcPredSec = clamp(fcPredSec, 430.0, 490.0)

        var dH1 = 0
        var dH2 = 0
        var dH3 = 0
        var dH4 = 0
        var dH5 = 0

        if (dens < 760) {
            dH1 = -30
            dH2 = -30
            dH3 = -20
            dH4 = 0
            dH5 = 0
        } else if (dens < 821) {
            dH1 = 0
            dH2 = 0
            dH3 = 0
            dH4 = 0
            dH5 = 0
        } else if (dens < 861) {
            dH1 = 30
            dH2 = 30
            dH3 = 20
            dH4 = 20
            dH5 = 20
        } else {
            dH1 = 60
            dH2 = 60
            dH3 = 40
            dH4 = 30
            dH5 = 30
        }

        var h1W = 1380 + dH1
        var h2W = 1320 + dH2
        var h3W = 1260 + dH3
        var h4W = max(1200, 1200 + dH4)
        var h5W = 1160 + dH5

        var devAdjust = 0
        var protectOffset = 25
        var devPa = when (input.orientation) {
            "clean" -> 16
            "stable" -> 14
            "thick" -> 12
            else -> 14
        }

        if (input.mode == "M2") {
            h4W = max(1200, 1200 + dH4 + 20)
            h5W = 1160 + dH5 + 20
            devAdjust = 8
            devPa = max(devPa - 1, 10)
        }

        if (input.mode == "M3") {
            protectOffset = 30
            devAdjust = -6
            devPa += 1
            if (dens < 760) {
                h3W = 1260 - 10
            }
        }

        if (input.mode == "M4") {
            devAdjust = 10
            devPa = max(devPa - 2, 10)
        }

        if (input.mode == "M5") {
            protectOffset = 20
        }

        var devTime = when (input.roastLevel) {
            "nordic" -> 75
            "light" -> 80
            "light_medium" -> 85
            "medium" -> 90
            "medium_dark" -> 100
            "dark" -> 115
            "french" -> 130
            else -> 85
        }

        when (input.purpose) {
            "soe" -> devTime += 5
            "espresso" -> devTime += 10
            "american" -> devTime += 3
        }

        devTime += devAdjust

        val dropSec = fcPredSec + devTime
        val dtr = devTime / dropSec * 100.0

        val rorBase = when {
            dens < 760 -> listOf(10.5, 8.3, 5.5, 4.8, 3.5)
            dens < 821 -> listOf(10.0, 7.8, 5.2, 4.5, 3.2)
            dens < 861 -> listOf(9.4, 7.2, 4.8, 4.2, 3.0)
            else -> listOf(8.8, 6.8, 4.5, 3.9, 2.8)
        }

        val mcAdj = when {
            moist < 10.0 -> -0.3
            moist < 11.0 -> 0.0
            moist < 11.8 -> 0.3
            else -> 0.6
        }

        val ptAdj = when (input.process) {
            "washed" -> listOf(0.0, 0.0, 0.0, 0.0, 0.0)
            "honey_washed" -> listOf(-0.2, -0.2, 0.0, 0.0, 0.0)
            "natural" -> listOf(-0.3, -0.3, -0.2, -0.2, -0.2)
            "honey_natural" -> listOf(-0.3, -0.3, -0.2, -0.2, -0.2)
            "anaerobic" -> listOf(-0.4, -0.3, -0.2, -0.2, -0.2)
            "fermented" -> listOf(-0.5, -0.3, -0.2, -0.2, -0.2)
            else -> listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val rorRaw = rorBase.mapIndexed { i, v ->
            v + if (i < 3) mcAdj else 0.0 + 0.0
        }.mapIndexed { i, v -> v + ptAdj[i] }

        val negCap = listOf(0.5, 0.5, 0.4, 0.3, 0.3)

        val rorCapped = rorRaw.mapIndexed { i, v ->
            val totalNeg = (if (i < 3) mcAdj else 0.0) + ptAdj[i]
            if (totalNeg < -negCap[i]) rorBase[i] - negCap[i] else v
        }

        val isM3HeavyProcess =
            input.mode == "M3" && (input.process == "anaerobic" || input.process == "fermented")

        val rorFloor = listOf(8.8, 6.6, 4.4, 3.3, 2.5)

        val rorFull5 = rorCapped.mapIndexed { i, v ->
            if (isM3HeavyProcess) max(v, rorFloor[i]) else v
        }

        val awTol = when {
            input.aw >= 0.60 -> 0.5
            input.aw >= 0.55 -> 0.3
            else -> 0.0
        }

        val rorTargets = listOf(rorFull5[0], rorFull5[1], rorFull5[2])

        val rorFull = listOf(
            rorFull5[0] + 6.0,
            rorFull5[0],
            rorFull5[1],
            rorFull5[2],
            rorFull5[4]
        )

        val m3LowDens = input.mode == "M3" && dens < 760
        val wind1Pa = if (m3LowDens) 16 else 15
        val wind2Pa = if (m3LowDens) 19 else 18

        return PlannerResult(
            chargeBT = chargeBT,
            rpm = rpm,
            preheatPa = preheatPa,
            devPa = devPa,
            fc1 = fc1,
            fc2 = fc2,
            fcPredSec = fcPredSec,
            devTime = devTime,
            dropSec = dropSec,
            dtrPercent = dtr,

            h1W = h1W,
            h2W = h2W,
            h3W = h3W,
            h4W = h4W,
            h5W = h5W,

            h1Sec = ttSec + 60.0,
            h2Sec = ttSec + 150.0,
            h3Sec = fcPredSec - 60.0,
            h4Sec = fcPredSec,
            h5Sec = fcPredSec + 40.0,

            wind1Sec = ttSec + 90.0,
            wind2Sec = ttSec + 180.0,
            protectSec = fcPredSec - protectOffset,
            wind1Pa = wind1Pa,
            wind2Pa = wind2Pa,

            rorFull = rorFull,
            rorFull5 = rorFull5,
            rorTargets = rorTargets,
            rorAnchors = listOf(
                ttSec + 60.0,
                ttSec + 90.0,
                ttSec + 180.0,
                fcPredSec - 90.0,
                fcPredSec + 40.0
            ),

            ptLabel = processLabel(input.process),
            awTol = awTol,
            m3Protected = isM3HeavyProcess,
            m3LowDens = m3LowDens
        )
    }
}
