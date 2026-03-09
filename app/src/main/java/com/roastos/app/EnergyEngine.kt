package com.roastos.app

import kotlin.math.abs

data class EnergySnapshot(
    val energyState: String,
    val score: Int,
    val heatLevel: String,
    val airflowLevel: String,
    val thermalMomentum: String,
    val rorTrend: String,
    val reason: String,
    val summary: String
)

object EnergyEngine {

    fun evaluate(
        machineProfile: MachineProfile,
        machineState: MachineState
    ): EnergySnapshot {

        val powerRatio = safeRatio(
            value = machineState.powerW - machineProfile.minPowerW,
            max = machineProfile.maxPowerW - machineProfile.minPowerW
        )

        val airflowRatio = safeRatio(
            value = machineState.airflowPa - machineProfile.minAirflowPa,
            max = machineProfile.maxAirflowPa - machineProfile.minAirflowPa
        )

        val rpmRatio = safeRatio(
            value = machineState.drumRpm - machineProfile.minDrumRpm,
            max = machineProfile.maxDrumRpm - machineProfile.minDrumRpm
        )

        val environmentLoad =
            (machineProfile.environmentSensitivity * buildEnvironmentPenalty(machineState))
                .coerceIn(0.0, 1.0)

        val heatContribution = powerRatio * 48.0
        val airflowPenalty = airflowRatio * 24.0
        val drumAssist = rpmRatio * 6.0
        val inertiaAssist = machineProfile.thermalInertia * 18.0
        val rorAssist = buildRorAssist(machineState.ror)

        var score = (
            heatContribution
                - airflowPenalty
                + drumAssist
                + inertiaAssist
                + rorAssist
                - environmentLoad * 12.0
            ).toInt()

        score = score.coerceIn(0, 100)

        val heatLevel = when {
            powerRatio >= 0.82 -> "Very High"
            powerRatio >= 0.62 -> "High"
            powerRatio >= 0.42 -> "Moderate"
            powerRatio >= 0.22 -> "Low"
            else -> "Very Low"
        }

        val airflowLevel = when {
            airflowRatio >= 0.78 -> "Very Strong"
            airflowRatio >= 0.58 -> "Strong"
            airflowRatio >= 0.36 -> "Moderate"
            airflowRatio >= 0.18 -> "Light"
            else -> "Minimal"
        }

        val thermalMomentum = when {
            machineProfile.thermalInertia >= 0.80 -> "Heavy Inertia"
            machineProfile.thermalInertia >= 0.60 -> "Moderate Inertia"
            else -> "Light Inertia"
        }

        val rorTrend = when {
            machineState.ror >= 12.0 -> "ROR Very Aggressive"
            machineState.ror >= 9.0 -> "ROR Strong"
            machineState.ror >= 6.0 -> "ROR Healthy"
            machineState.ror >= 3.5 -> "ROR Soft"
            machineState.ror > 0.0 -> "ROR Weak"
            else -> "ROR Falling"
        }

        val energyState = when {
            score >= 80 && machineState.ror >= 8.0 -> "High Energy"
            score >= 65 && machineState.ror >= 5.0 -> "Balanced Energy"
            score >= 50 -> "Moderate Energy"
            score >= 35 -> "Low Energy"
            else -> "Energy Deficit"
        }

        val reasons = mutableListOf<String>()

        when {
            powerRatio >= 0.75 -> reasons.add("heat input is high")
            powerRatio <= 0.20 -> reasons.add("heat input is low")
            else -> reasons.add("heat input is moderate")
        }

        when {
            airflowRatio >= 0.70 -> reasons.add("airflow is strongly extracting heat")
            airflowRatio <= 0.15 -> reasons.add("airflow is preserving heat")
            else -> reasons.add("airflow impact is moderate")
        }

        when {
            machineState.ror >= 9.0 -> reasons.add("ROR indicates strong momentum")
            machineState.ror in 4.0..8.999 -> reasons.add("ROR indicates workable momentum")
            machineState.ror > 0.0 -> reasons.add("ROR indicates soft momentum")
            else -> reasons.add("ROR indicates falling momentum")
        }

        if (environmentLoad > 0.45) {
            reasons.add("environmental conditions are adding drag")
        }

        if (machineProfile.thermalInertia >= 0.60) {
            reasons.add("machine inertia is buffering changes")
        }

        val reason = reasons.joinToString(" | ")

        val summary = """
Energy Engine v2

Machine
${machineProfile.name}

Energy State
$energyState

Score
$score

Heat Level
$heatLevel

Airflow Level
$airflowLevel

Thermal Momentum
$thermalMomentum

ROR Trend
$rorTrend

Reason
$reason
        """.trimIndent()

        return EnergySnapshot(
            energyState = energyState,
            score = score,
            heatLevel = heatLevel,
            airflowLevel = airflowLevel,
            thermalMomentum = thermalMomentum,
            rorTrend = rorTrend,
            reason = reason,
            summary = summary
        )
    }

    private fun safeRatio(
        value: Int,
        max: Int
    ): Double {
        if (max <= 0) return 0.0
        return value.toDouble().div(max.toDouble()).coerceIn(0.0, 1.0)
    }

    private fun buildEnvironmentPenalty(
        machineState: MachineState
    ): Double {
        val tempPenalty = when {
            machineState.environmentTemp <= 10.0 -> 1.0
            machineState.environmentTemp <= 16.0 -> 0.7
            machineState.environmentTemp <= 22.0 -> 0.35
            else -> 0.15
        }

        val humidityPenalty = when {
            machineState.environmentHumidity >= 80.0 -> 0.55
            machineState.environmentHumidity >= 65.0 -> 0.35
            machineState.environmentHumidity >= 45.0 -> 0.18
            else -> 0.08
        }

        return (tempPenalty + humidityPenalty).coerceIn(0.0, 1.2)
    }

    private fun buildRorAssist(
        ror: Double
    ): Double {
        return when {
            ror >= 12.0 -> 20.0
            ror >= 9.0 -> 15.0
            ror >= 6.0 -> 10.0
            ror >= 3.5 -> 4.0
            ror > 0.0 -> -4.0
            else -> -12.0
        }
    }

    fun thermalCorrectionHint(
        machineProfile: MachineProfile,
        machineState: MachineState
    ): String {
        val energy = evaluate(machineProfile, machineState)

        return when {
            energy.energyState == "Energy Deficit" && machineState.ror < 3.5 ->
                "Add heat earlier and avoid excessive airflow"

            energy.energyState == "High Energy" && machineState.ror > 10.0 ->
                "Reduce heat slightly and prepare to widen airflow"

            energy.airflowLevel == "Very Strong" && machineState.ror < 5.0 ->
                "Airflow may be stripping too much heat"

            energy.thermalMomentum == "Heavy Inertia" && abs(machineState.ror) < 4.0 ->
                "Make smaller earlier corrections because inertia is high"

            else ->
                "Energy condition is acceptable"
        }
    }
}
