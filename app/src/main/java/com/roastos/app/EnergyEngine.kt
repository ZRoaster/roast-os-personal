package com.roastos.app

import kotlin.math.abs

enum class EnergyState {
    HIGH,
    BALANCED,
    MODERATE,
    LOW,
    DEFICIT
}

data class EnergySnapshot(
    val energyState: String,
    val stateEnum: EnergyState,
    val score: Int,
    val heatLevel: String,
    val airflowLevel: String,
    val thermalMomentum: String,
    val rorTrend: String,
    val reason: String,
    val summary: String,

    // 兼容旧系统
    val energyError: Double = 0.0
)

object EnergyEngine {

    fun evaluate(
        machineProfile: MachineProfile,
        machineState: MachineState
    ): EnergySnapshot {

        val powerRatio = safeRatio(
            machineState.powerW - machineProfile.minPowerW,
            machineProfile.maxPowerW - machineProfile.minPowerW
        )

        val airflowRatio = safeRatio(
            machineState.airflowPa - machineProfile.minAirflowPa,
            machineProfile.maxAirflowPa - machineProfile.minAirflowPa
        )

        val rpmRatio = safeRatio(
            machineState.drumRpm - machineProfile.minDrumRpm,
            machineProfile.maxDrumRpm - machineProfile.minDrumRpm
        )

        val environmentLoad =
            (machineProfile.environmentSensitivity *
                    buildEnvironmentPenalty(machineState))
                .coerceIn(0.0, 1.0)

        val heatContribution = powerRatio * 48
        val airflowPenalty = airflowRatio * 24
        val drumAssist = rpmRatio * 6
        val inertiaAssist = machineProfile.thermalInertia * 18
        val rorAssist = buildRorAssist(machineState.ror)

        var score = (
                heatContribution
                        - airflowPenalty
                        + drumAssist
                        + inertiaAssist
                        + rorAssist
                        - environmentLoad * 12
                ).toInt()

        score = score.coerceIn(0, 100)

        val heatLevel = when {
            powerRatio > 0.8 -> "Very High"
            powerRatio > 0.6 -> "High"
            powerRatio > 0.4 -> "Moderate"
            powerRatio > 0.2 -> "Low"
            else -> "Very Low"
        }

        val airflowLevel = when {
            airflowRatio > 0.75 -> "Very Strong"
            airflowRatio > 0.55 -> "Strong"
            airflowRatio > 0.35 -> "Moderate"
            airflowRatio > 0.15 -> "Light"
            else -> "Minimal"
        }

        val thermalMomentum = when {
            machineProfile.thermalInertia > 0.8 -> "Heavy Inertia"
            machineProfile.thermalInertia > 0.6 -> "Moderate Inertia"
            else -> "Light Inertia"
        }

        val rorTrend = when {
            machineState.ror >= 12 -> "ROR Very Aggressive"
            machineState.ror >= 9 -> "ROR Strong"
            machineState.ror >= 6 -> "ROR Healthy"
            machineState.ror >= 3 -> "ROR Soft"
            machineState.ror > 0 -> "ROR Weak"
            else -> "ROR Falling"
        }

        val stateEnum = when {
            score >= 80 && machineState.ror >= 8 -> EnergyState.HIGH
            score >= 65 && machineState.ror >= 5 -> EnergyState.BALANCED
            score >= 50 -> EnergyState.MODERATE
            score >= 35 -> EnergyState.LOW
            else -> EnergyState.DEFICIT
        }

        val energyState = when (stateEnum) {
            EnergyState.HIGH -> "High Energy"
            EnergyState.BALANCED -> "Balanced Energy"
            EnergyState.MODERATE -> "Moderate Energy"
            EnergyState.LOW -> "Low Energy"
            EnergyState.DEFICIT -> "Energy Deficit"
        }

        val reason =
            "heat=$heatLevel | airflow=$airflowLevel | ror=$rorTrend"

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
""".trimIndent()

        return EnergySnapshot(
            energyState,
            stateEnum,
            score,
            heatLevel,
            airflowLevel,
            thermalMomentum,
            rorTrend,
            reason,
            summary,

            // 兼容旧系统
            energyError = abs(50 - score) / 50.0
        )
    }

    private fun safeRatio(value: Int, max: Int): Double {
        if (max <= 0) return 0.0
        return value.toDouble() / max.toDouble()
    }

    private fun buildEnvironmentPenalty(
        machineState: MachineState
    ): Double {

        val tempPenalty = when {
            machineState.environmentTemp < 10 -> 1.0
            machineState.environmentTemp < 16 -> 0.7
            machineState.environmentTemp < 22 -> 0.35
            else -> 0.15
        }

        val humidityPenalty = when {
            machineState.environmentHumidity > 80 -> 0.55
            machineState.environmentHumidity > 65 -> 0.35
            machineState.environmentHumidity > 45 -> 0.18
            else -> 0.08
        }

        return (tempPenalty + humidityPenalty)
    }

    private fun buildRorAssist(ror: Double): Double {
        return when {
            ror >= 12 -> 20.0
            ror >= 9 -> 15.0
            ror >= 6 -> 10.0
            ror >= 3 -> 4.0
            ror > 0 -> -4.0
            else -> -12.0
        }
    }
}
