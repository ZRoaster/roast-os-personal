package com.roastos.app

data class EnvironmentCompensationResult(
    val heatRetentionOffset: Double,
    val dryingOffset: Double,
    val airflowEfficiencyOffset: Double,
    val pressureOffset: Double,
    val summaryText: String
)

object EnvironmentCompensationEngine {

    fun evaluate(
        profile: EnvironmentProfile = EnvironmentProfileEngine.current()
    ): EnvironmentCompensationResult {

        val temp = profile.ambientTempC ?: 25.0
        val humidity = profile.ambientHumidityRh ?: 50.0
        val altitude = profile.altitudeMeters ?: 0
        val pressure = profile.barometricPressureHpa ?: 1013.25

        val heatRetentionOffset = buildHeatRetentionOffset(
            ambientTempC = temp,
            altitudeMeters = altitude
        )

        val dryingOffset = buildDryingOffset(
            ambientHumidityRh = humidity,
            ambientTempC = temp
        )

        val airflowEfficiencyOffset = buildAirflowEfficiencyOffset(
            altitudeMeters = altitude,
            barometricPressureHpa = pressure,
            ambientHumidityRh = humidity
        )

        val pressureOffset = buildPressureOffset(
            altitudeMeters = altitude,
            barometricPressureHpa = pressure
        )

        return EnvironmentCompensationResult(
            heatRetentionOffset = heatRetentionOffset,
            dryingOffset = dryingOffset,
            airflowEfficiencyOffset = airflowEfficiencyOffset,
            pressureOffset = pressureOffset,
            summaryText = buildSummary(
                heatRetentionOffset = heatRetentionOffset,
                dryingOffset = dryingOffset,
                airflowEfficiencyOffset = airflowEfficiencyOffset,
                pressureOffset = pressureOffset
            )
        )
    }

    fun summary(): String {
        return evaluate().summaryText
    }

    private fun buildHeatRetentionOffset(
        ambientTempC: Double,
        altitudeMeters: Int
    ): Double {
        val tempOffset = (ambientTempC - 25.0) * 0.04
        val altitudeOffset = altitudeMeters * -0.00008
        return (tempOffset + altitudeOffset).coerceIn(-2.0, 2.0)
    }

    private fun buildDryingOffset(
        ambientHumidityRh: Double,
        ambientTempC: Double
    ): Double {
        val humidityOffset = (ambientHumidityRh - 50.0) * 0.03
        val tempOffset = (25.0 - ambientTempC) * 0.01
        return (humidityOffset + tempOffset).coerceIn(-2.0, 2.0)
    }

    private fun buildAirflowEfficiencyOffset(
        altitudeMeters: Int,
        barometricPressureHpa: Double,
        ambientHumidityRh: Double
    ): Double {
        val altitudeOffset = altitudeMeters * -0.00012
        val pressureDelta = (barometricPressureHpa - 1013.25) * 0.002
        val humidityOffset = (ambientHumidityRh - 50.0) * -0.005
        return (altitudeOffset + pressureDelta + humidityOffset).coerceIn(-2.0, 2.0)
    }

    private fun buildPressureOffset(
        altitudeMeters: Int,
        barometricPressureHpa: Double
    ): Double {
        val altitudeComponent = altitudeMeters * -0.00015
        val pressureComponent = (barometricPressureHpa - 1013.25) * 0.003
        return (altitudeComponent + pressureComponent).coerceIn(-2.0, 2.0)
    }

    private fun buildSummary(
        heatRetentionOffset: Double,
        dryingOffset: Double,
        airflowEfficiencyOffset: Double,
        pressureOffset: Double
    ): String {
        return """
Environment Compensation

Heat Retention
${formatOffset(heatRetentionOffset)}

Drying
${formatOffset(dryingOffset)}

Airflow Efficiency
${formatOffset(airflowEfficiencyOffset)}

Pressure
${formatOffset(pressureOffset)}
        """.trimIndent()
    }

    private fun formatOffset(value: Double): String {
        return if (value >= 0) {
            "+${String.format("%.2f", value)}"
        } else {
            String.format("%.2f", value)
        }
    }
}
