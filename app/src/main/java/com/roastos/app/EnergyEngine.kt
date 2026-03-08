package com.roastos.app

data class EnergyState(
    val targetRor: Double,
    val predictedRor: Double,
    val energyError: Double,
    val beanLoad: Double,
    val envLoad: Double,
    val machineEffect: Double,
    val controlEffect: Double,
    val phaseEffect: Double,
    val summary: String
)

object EnergyEngine {

    fun evaluate(): EnergyState {

        val bean = RoastStateModel.bean
        val machine = RoastStateModel.machine
        val environment = RoastStateModel.environment
        val control = RoastStateModel.control
        val roast = RoastStateModel.roast
        val calibration = RoastStateModel.calibration

        val baseTargetRor = when (roast.phase) {
            "Drying" -> 16.0
            "Maillard / Pre-FC" -> 12.0
            "Development" -> 7.0
            else -> 12.0
        }

        val beanLoad = calculateBeanLoad(
            density = bean.density,
            moisture = bean.moisture,
            aw = bean.aw
        )

        val envLoad = calculateEnvironmentLoad(
            ambientTemp = environment.ambientTemp,
            ambientHumidity = environment.ambientHumidity,
            ambientPressure = environment.ambientPressure
        )

        val machineEffect = calculateMachineEffect(
            thermalMass = machine.thermalMass,
            drumMass = machine.drumMass,
            heatRetention = machine.heatRetention,
            machineResponseFactor = calibration.machineResponseFactor
        )

        val controlEffect = calculateControlEffect(
            powerW = control.powerW,
            airflowPa = control.airflowPa,
            drumRpm = control.drumRpm,
            maxPowerW = machine.maxPowerW,
            maxAirPa = machine.maxAirPa,
            maxRpm = machine.maxRpm
        )

        val phaseEffect = calculatePhaseEffect(roast.phase)

        val targetRor =
            baseTargetRor +
            machineEffect -
            beanLoad -
            envLoad +
            calibration.rorBias

        val predictedRor =
            roast.ror +
            controlEffect +
            machineEffect -
            beanLoad -
            envLoad -
            phaseEffect +
            calibration.rorBias

        val boundedTargetRor = targetRor.coerceIn(4.0, 22.0)
        val boundedPredictedRor = predictedRor.coerceIn(0.0, 25.0)

        val energyError = boundedTargetRor - boundedPredictedRor

        val summary = when {
            energyError > 2.0 ->
                "Energy below target. System likely needs more support."
            energyError < -2.0 ->
                "Energy above target. System likely needs trimming."
            else ->
                "Energy near target window."
        }

        return EnergyState(
            targetRor = boundedTargetRor,
            predictedRor = boundedPredictedRor,
            energyError = energyError,
            beanLoad = beanLoad,
            envLoad = envLoad,
            machineEffect = machineEffect,
            controlEffect = controlEffect,
            phaseEffect = phaseEffect,
            summary = summary
        )
    }

    private fun calculateBeanLoad(
        density: Double,
        moisture: Double,
        aw: Double
    ): Double {

        val densityTerm = ((density - 800.0) / 100.0) * 0.8
        val moistureTerm = (moisture - 10.5) * 0.7
        val awTerm = (aw - 0.55) * 5.0

        return densityTerm + moistureTerm + awTerm
    }

    private fun calculateEnvironmentLoad(
        ambientTemp: Double,
        ambientHumidity: Double,
        ambientPressure: Double
    ): Double {

        val tempTerm = (20.0 - ambientTemp) * 0.08
        val humidityTerm = (ambientHumidity - 50.0) * 0.015
        val pressureTerm = (1013.0 - ambientPressure) * 0.002

        return tempTerm + humidityTerm + pressureTerm
    }

    private fun calculateMachineEffect(
        thermalMass: Double,
        drumMass: Double,
        heatRetention: Double,
        machineResponseFactor: Double
    ): Double {

        val thermalTerm = (thermalMass - 1.0) * 0.8
        val drumTerm = (drumMass - 1.0) * 0.4
        val retentionTerm = (heatRetention - 1.0) * 0.8
        val responseTerm = (machineResponseFactor - 1.0) * 0.6

        return thermalTerm + drumTerm + retentionTerm + responseTerm
    }

    private fun calculateControlEffect(
        powerW: Int,
        airflowPa: Int,
        drumRpm: Int,
        maxPowerW: Int,
        maxAirPa: Int,
        maxRpm: Int
    ): Double {

        val powerRatio =
            if (maxPowerW > 0) powerW.toDouble() / maxPowerW.toDouble() else 0.0

        val airRatio =
            if (maxAirPa > 0) airflowPa.toDouble() / maxAirPa.toDouble() else 0.0

        val rpmRatio =
            if (maxRpm > 0) drumRpm.toDouble() / maxRpm.toDouble() else 0.0

        val powerTerm = powerRatio * 8.0
        val airTerm = airRatio * 3.5
        val rpmTerm = rpmRatio * 1.2

        return powerTerm - airTerm + rpmTerm
    }

    private fun calculatePhaseEffect(phase: String): Double {
        return when (phase) {
            "Drying" -> 1.0
            "Maillard / Pre-FC" -> 1.8
            "Development" -> 2.5
            else -> 1.5
        }
    }
}
