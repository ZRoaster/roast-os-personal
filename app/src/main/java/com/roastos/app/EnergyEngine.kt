package com.roastos.app

data class EnergyState(
    val targetRor: Double,
    val predictedRor: Double,
    val energyError: Double
)

object EnergyEngine {

    private const val HEAT_DELAY = 28
    private const val AIR_DELAY = 18

    fun evaluate(
        phase: String,
        currentRor: Double,
        envTemp: Double,
        humidity: Double,
        density: Double,
        mc: Double,
        aw: Double
    ): EnergyState {

        val baseTarget = when (phase) {

            "Drying" -> 16.0
            "Maillard / Pre-FC" -> 12.0
            "Development" -> 7.0
            else -> 12.0
        }

        val envFactor =
            (20 - envTemp) * 0.05 +
            (humidity - 40) * 0.01

        val beanFactor =
            (density - 800) * 0.002 +
            (mc - 11) * 0.4 +
            (aw - 0.55) * 4

        val targetRor =
            baseTarget +
            envFactor +
            beanFactor

        val predictedFutureRor =
            currentRor * 0.92

        val energyError =
            targetRor - predictedFutureRor

        return EnergyState(
            targetRor,
            predictedFutureRor,
            energyError
        )
    }
}
