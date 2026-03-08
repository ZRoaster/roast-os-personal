package com.roastos.app

import kotlin.math.abs

data class PreheatTarget(
    val targetTempC: Double,
    val windowLowC: Double,
    val windowHighC: Double,
    val holdSec: Int,
    val intent: String,
    val reason: String
)

data class PreheatLiveInput(
    val currentTempC: Double,
    val riseRateCPerSec: Double,
    val currentPowerW: Int,
    val elapsedSec: Int,
    val holdElapsedSec: Int = 0,
    val ambientTempC: Double,
    val ambientRh: Double
)

data class PreheatAssistResult(
    val stage: String,
    val action: String,
    val suggestedPowerDeltaW: Int,
    val beep: Boolean,
    val beepText: String,
    val countdownText: String,
    val statusText: String,
    val summary: String
)

object RoastPreheatAssistEngine {

    fun buildTargetFromCurrentState(): PreheatTarget {
        val input = AppState.lastPlannerInput
        val profileSuggestion = latestProfileSuggestionOrNull()

        val density = input?.density ?: 800.0
        val moisture = input?.moisture ?: 10.5
        val aw = input?.aw ?: 0.50
        val envTemp = input?.envTemp ?: 25.0
        val envRh = input?.envRH ?: 50.0
        val process = input?.process ?: ""
        val mode = input?.mode ?: ""
        val roastLevel = input?.roastLevel ?: ""
        val orientation = input?.orientation ?: ""

        var target = 210.0
        var holdSec = 120
        val reasons = mutableListOf<String>()

        if (density >= 830) {
            target += 1.5
            reasons.add("High density → slightly higher preheat")
        } else if (density <= 780) {
            target -= 1.0
            reasons.add("Lower density → slightly softer preheat")
        }

        if (moisture >= 11.3) {
            target += 1.0
            holdSec += 15
            reasons.add("Higher moisture → more stored heat and slightly longer hold")
        } else if (moisture <= 10.0) {
            target -= 0.5
            reasons.add("Lower moisture → avoid overly aggressive front-end")
        }

        if (aw >= 0.57) {
            target += 0.5
            reasons.add("Higher aw → support front-end energy")
        } else if (aw <= 0.45) {
            target -= 0.5
            reasons.add("Lower aw → avoid over-pushing early stage")
        }

        if (envTemp <= 20.0) {
            target += 1.0
            holdSec += 15
            reasons.add("Cool ambient temperature → compensate heat loss")
        } else if (envTemp >= 30.0) {
            target -= 0.5
            reasons.add("Warm ambient temperature → reduce overshoot tendency")
        }

        if (envRh >= 70.0) {
            holdSec += 15
            reasons.add("Higher ambient RH → slightly longer stabilization")
        } else if (envRh <= 35.0) {
            target -= 0.5
            reasons.add("Dry ambient air → avoid too sharp front-end response")
        }

        if (process.contains("washed", ignoreCase = true)) {
            target += 0.5
            reasons.add("Washed profile → preserve clarity with stable front-end")
        }

        if (orientation.contains("stable", ignoreCase = true)) {
            holdSec += 15
            reasons.add("Stable replay intent → longer hold for repeatability")
        }

        if (roastLevel.contains("light", ignoreCase = true)) {
            target += 0.5
            reasons.add("Lighter roast target → support early structure")
        }

        if (mode.contains("M2", ignoreCase = true)) {
            reasons.add("M2 mode baseline applied")
        }

        if (profileSuggestion != null) {
            val profileFc = profileSuggestion.suggestedFcSec
            if (profileFc != null && profileFc <= 450) {
                target -= 0.5
                reasons.add("Profile suggests faster roast rhythm → slightly softer preheat")
            } else if (profileFc != null && profileFc >= 510) {
                target += 0.5
                reasons.add("Profile suggests slower roast rhythm → slightly stronger preheat")
            }
        }

        target = target.coerceIn(202.0, 215.0)
        holdSec = holdSec.coerceIn(60, 180)

        val windowLow = target - 1.0
        val windowHigh = target + 1.0

        val intent = when {
            target >= 212.0 -> "Stronger front-end energy"
            target <= 207.5 -> "Softer front-end energy"
            else -> "Balanced front-end energy"
        }

        return PreheatTarget(
            targetTempC = target,
            windowLowC = windowLow,
            windowHighC = windowHigh,
            holdSec = holdSec,
            intent = intent,
            reason = reasons.joinToString("; ").ifBlank { "Baseline preheat recommendation" }
        )
    }

    fun assess(
        target: PreheatTarget,
        live: PreheatLiveInput
    ): PreheatAssistResult {
        val temp = live.currentTempC
        val rise = live.riseRateCPerSec
        val power = live.currentPowerW
        val low = target.windowLowC
        val high = target.windowHighC
        val center = target.targetTempC

        val predicted5s = temp + rise * 5.0
        val predicted10s = temp + rise * 10.0

        val inWindow = temp in low..high
        val overshootRisk = predicted10s > high + 0.3
        val undershootRisk = temp < low && rise <= 0.02
        val closeToWindow = abs(temp - center) <= 3.0
        val holdReady = inWindow && abs(rise) <= 0.08

        val stage: String
        val action: String
        val delta: Int
        val beep: Boolean
        val beepText: String
        val countdownText: String

        if (temp < low - 3.0) {
            stage = "Climb"
            action = "Maintain climb"
            delta = 0
            beep = false
            beepText = ""
            countdownText = estimateCountdown(center, temp, rise)
        } else if (closeToWindow && overshootRisk) {
            stage = "Approach Window"
            action = "Reduce heat now"
            delta = suggestReduction(power, rise, temp, center)
            beep = true
            beepText = "BEEP  Reduce heat ≈ ${formatDelta(delta)}W  Prevent overshoot"
            countdownText = estimateCountdown(center, temp, rise)
        } else if (temp > high) {
            stage = "Overshoot Recovery"
            action = "Reduce heat and let temperature settle"
            delta = suggestStrongReduction(power, rise)
            beep = true
            beepText = "BEEP  Overshoot detected  Reduce heat ≈ ${formatDelta(delta)}W"
            countdownText = "Wait for re-entry into window"
        } else if (inWindow && !holdReady) {
            stage = "Window Stabilizing"
            action = if (rise > 0.08) "Reduce heat slightly" else "Maintain or micro-correct"
            delta = if (rise > 0.08) suggestMicroReduction(power, rise) else 0
            beep = rise > 0.08
            beepText = if (beep) {
                "BEEP  In window but still rising  Reduce heat ≈ ${formatDelta(delta)}W"
            } else {
                ""
            }
            countdownText = "Stabilizing before hold"
        } else if (holdReady && live.holdElapsedSec < target.holdSec) {
            val remain = target.holdSec - live.holdElapsedSec
            val holdingAction = when {
                predicted5s > high -> "Reduce heat slightly"
                predicted5s < low || undershootRisk -> "Increase heat slightly"
                else -> "Maintain hold"
            }

            val holdingDelta = when {
                predicted5s > high -> suggestMicroReduction(power, rise)
                predicted5s < low || undershootRisk -> suggestMicroIncrease(power, rise)
                else -> 0
            }

            val preDropWarn = remain in 1..10

            stage = "Holding"
            action = holdingAction
            delta = holdingDelta
            beep = preDropWarn || holdingDelta != 0
            beepText = when {
                preDropWarn -> "BEEP  Charge in $remain seconds  Prepare beans"
                holdingDelta < 0 -> "BEEP  Hold drifting high  Reduce heat ≈ ${formatDelta(holdingDelta)}W"
                holdingDelta > 0 -> "BEEP  Hold drifting low  Increase heat ≈ +${holdingDelta}W"
                else -> ""
            }
            countdownText = "Charge in ${remain}s"
        } else if (holdReady && live.holdElapsedSec >= target.holdSec) {
            stage = "Charge Ready"
            action = "Drop beans now"
            delta = 0
            beep = true
            beepText = "BEEP  Preheat hold complete  Drop beans now"
            countdownText = "Ready"
        } else {
            stage = "Transition"
            action = "Observe and micro-correct"
            delta = 0
            beep = false
            beepText = ""
            countdownText = estimateCountdown(center, temp, rise)
        }

        val statusText = """
Stage
$stage

Current
Temp ${"%.1f".format(temp)}℃
Rise ${"%.2f".format(rise)}℃/s
Power ${power}W

Target
${"%.1f".format(center)}℃  (${formatWindow(low, high)})
Hold ${target.holdSec}s

Action
$action
        """.trimIndent()

        val summary = """
Preheat Assist

Target
${"%.1f".format(center)}℃

Window
${formatWindow(low, high)}

Hold
${target.holdSec}s

Intent
${target.intent}

Stage
$stage

Action
$action

Suggested Power Change
${formatDeltaText(delta)}

Countdown
$countdownText

Beep
${if (beep) beepText else "-"}
        """.trimIndent()

        return PreheatAssistResult(
            stage = stage,
            action = action,
            suggestedPowerDeltaW = delta,
            beep = beep,
            beepText = beepText,
            countdownText = countdownText,
            statusText = statusText,
            summary = summary
        )
    }

    private fun latestProfileSuggestionOrNull(): RoastProfilePlanSuggestion? {
        val latest = RoastProfileEngine.latest() ?: return null
        return RoastProfilePlannerBridge.buildFromProfile(latest)
    }

    private fun estimateCountdown(target: Double, current: Double, rise: Double): String {
        if (rise <= 0.02) return "Rise too low to estimate"
        val sec = ((target - current) / rise).toInt()
        return if (sec <= 0) "Near target" else "Target window in ~${sec}s"
    }

    private fun suggestReduction(currentPower: Int, rise: Double, temp: Double, target: Double): Int {
        var delta = -80
        if (rise >= 0.25) delta = -120
        if (rise >= 0.40) delta = -160
        if (temp > target + 0.5) delta -= 20
        return delta
    }

    private fun suggestStrongReduction(currentPower: Int, rise: Double): Int {
        var delta = -120
        if (rise >= 0.20) delta = -160
        if (rise >= 0.35) delta = -200
        return delta
    }

    private fun suggestMicroReduction(currentPower: Int, rise: Double): Int {
        var delta = -40
        if (rise >= 0.12) delta = -60
        if (rise >= 0.20) delta = -80
        return delta
    }

    private fun suggestMicroIncrease(currentPower: Int, rise: Double): Int {
        var delta = 40
        if (rise < -0.05) delta = 60
        if (rise < -0.10) delta = 80
        return delta
    }

    private fun formatWindow(low: Double, high: Double): String {
        return "${"%.1f".format(low)}–${"%.1f".format(high)}℃"
    }

    private fun formatDelta(delta: Int): String {
        return if (delta >= 0) "+$delta" else delta.toString()
    }

    private fun formatDeltaText(delta: Int): String {
        return when {
            delta > 0 -> "+${delta}W"
            delta < 0 -> "${delta}W"
            else -> "Maintain"
        }
    }
}
