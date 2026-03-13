package com.roastos.app

import kotlin.math.roundToInt

data class RoastControlAdjustment(
    val recommendedHeatBiasPercent: Int,
    val recommendedAirflowBiasSteps: Int,
    val reason: String
) {
    fun summaryText(): String {
        return """
Heat Bias
${if (recommendedHeatBiasPercent >= 0) "+" else ""}$recommendedHeatBiasPercent%

Airflow Bias
${if (recommendedAirflowBiasSteps >= 0) "+" else ""}$recommendedAirflowBiasSteps

Reason
$reason
        """.trimIndent()
    }
}

object RoastControlModel {

    fun evaluate(): RoastControlAdjustment {
        val envComp = EnvironmentCompensationEngine.evaluate()
        val dynamics = MachineDynamicsEngine.currentAdjustedForEnvironment()

        val heatBias = buildHeatBias(envComp, dynamics)
        val airflowBias = buildAirflowBias(envComp, dynamics)
        val reason = buildReason(envComp, dynamics, heatBias, airflowBias)

        return RoastControlAdjustment(
            recommendedHeatBiasPercent = heatBias,
            recommendedAirflowBiasSteps = airflowBias,
            reason = reason
        )
    }

    private fun buildHeatBias(
        env: EnvironmentCompensationResult,
        dynamics: MachineCalibrationProfile
    ): Int {
        var score = 0.0

        score += env.heatRetentionOffset * -4.0
        score += env.dryingOffset * 2.0

        val heatUpDelay = dynamics.delays.heatUpDelaySec ?: 0.0
        val heatDownDelay = dynamics.delays.heatDownDelaySec ?: 0.0
        val thermalInertia = dynamics.inertia.thermalInertiaScore ?: 0.0

        if (heatUpDelay >= 10.0) score += 2.0
        if (heatUpDelay <= 6.0) score -= 1.0

        if (heatDownDelay >= 12.0) score -= 1.0

        score += (thermalInertia - 0.5) * -4.0

        return score.roundToInt().coerceIn(-8, 8)
    }

    private fun buildAirflowBias(
        env: EnvironmentCompensationResult,
        dynamics: MachineCalibrationProfile
    ): Int {
        var score = 0.0

        score += env.airflowEfficiencyOffset * -2.5
        score += env.pressureOffset * -1.5

        val airflowDelay = dynamics.delays.airflowDelaySec ?: 0.0
        val airflowInertia = dynamics.inertia.airflowInertiaScore ?: 0.0

        if (airflowDelay >= 4.0) score += 1.0
        if (airflowDelay <= 2.0) score -= 0.5

        score += (airflowInertia - 0.4) * -2.0

        return score.roundToInt().coerceIn(-2, 2)
    }

    private fun buildReason(
        env: EnvironmentCompensationResult,
        dynamics: MachineCalibrationProfile,
        heatBias: Int,
        airflowBias: Int
    ): String {
        val parts = mutableListOf<String>()

        if (env.heatRetentionOffset <= -0.5) {
            parts += "environment suggests weaker heat retention"
        } else if (env.heatRetentionOffset >= 0.5) {
            parts += "environment suggests stronger heat retention"
        }

        if (env.dryingOffset >= 0.5) {
            parts += "drying may run slower"
        } else if (env.dryingOffset <= -0.5) {
            parts += "drying may run faster"
        }

        if (env.airflowEfficiencyOffset <= -0.5) {
            parts += "airflow efficiency looks reduced"
        } else if (env.airflowEfficiencyOffset >= 0.5) {
            parts += "airflow efficiency looks favorable"
        }

        val heatUpDelay = dynamics.delays.heatUpDelaySec ?: 0.0
        val airflowDelay = dynamics.delays.airflowDelaySec ?: 0.0

        if (heatUpDelay >= 10.0) {
            parts += "machine heat response looks slower"
        }

        if (airflowDelay >= 4.0) {
            parts += "machine airflow response looks slower"
        }

        if (parts.isEmpty()) {
            parts += "environment and machine state are near baseline"
        }

        parts += "final bias: heat ${formatSignedPercent(heatBias)}, airflow ${formatSignedStep(airflowBias)}"

        return parts.joinToString("; ")
    }

    private fun formatSignedPercent(value: Int): String {
        return if (value >= 0) "+${value}%" else "${value}%"
    }

    private fun formatSignedStep(value: Int): String {
        return if (value >= 0) "+$value" else "$value"
    }
}
