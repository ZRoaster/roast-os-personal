package com.roastos.app

import kotlin.math.abs

data class RoastCalibrationMatchResult(
    val matchedProfile: MachineCalibrationProfile?,
    val score: Double,
    val reason: String
) {
    fun summaryText(): String {
        return """
Matched Calibration
${matchedProfile?.machineName ?: "-"}

Calibration ID
${matchedProfile?.calibrationId ?: "-"}

Score
${"%.2f".format(score)}

Reason
$reason
        """.trimIndent()
    }
}

object RoastCalibrationMatcherEngine {

    fun matchBest(
        machineId: String = "hb_m2se_default",
        currentEnvironment: EnvironmentProfile = EnvironmentProfileEngine.current()
    ): RoastCalibrationMatchResult {

        val candidates = RoastCalibrationHistoryEngine.findByMachineId(machineId)

        if (candidates.isEmpty()) {
            return RoastCalibrationMatchResult(
                matchedProfile = null,
                score = Double.MAX_VALUE,
                reason = "No calibration history for machine."
            )
        }

        val ranked = candidates
            .map { profile ->
                profile to scoreEnvironmentDistance(
                    current = currentEnvironment,
                    reference = profile.calibrationEnvironment
                )
            }
            .sortedBy { it.second }

        val best = ranked.first()
        val reason = buildReason(
            currentEnvironment,
            best.first.calibrationEnvironment,
            best.second
        )

        return RoastCalibrationMatchResult(
            matchedProfile = best.first,
            score = best.second,
            reason = reason
        )
    }

    private fun scoreEnvironmentDistance(
        current: EnvironmentProfile,
        reference: EnvironmentProfile
    ): Double {
        val tempDiff = abs(
            toDoubleValue(current.ambientTempC) - toDoubleValue(reference.ambientTempC)
        )

        val humidityDiff = abs(
            toDoubleValue(current.ambientHumidityRh) - toDoubleValue(reference.ambientHumidityRh)
        )

        val altitudeDiff = abs(
            toDoubleValue(current.altitudeMeters) - toDoubleValue(reference.altitudeMeters)
        )

        return tempDiff * 1.0 +
            humidityDiff * 0.08 +
            altitudeDiff * 0.01
    }

    private fun buildReason(
        current: EnvironmentProfile,
        reference: EnvironmentProfile,
        score: Double
    ): String {
        return """
Current env: ${current.ambientTempC ?: "-"} °C, ${current.ambientHumidityRh ?: "-"} %RH, ${current.altitudeMeters ?: "-"} m
Matched env: ${reference.ambientTempC ?: "-"} °C, ${reference.ambientHumidityRh ?: "-"} %RH, ${reference.altitudeMeters ?: "-"} m
Environment distance score: ${"%.2f".format(score)}
        """.trimIndent()
    }

    private fun toDoubleValue(value: Any?): Double {
        return when (value) {
            null -> 0.0
            is Double -> value
            is Float -> value.toDouble()
            is Int -> value.toDouble()
            is Long -> value.toDouble()
            is Short -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> value.toString().toDoubleOrNull() ?: 0.0
        }
    }
}
