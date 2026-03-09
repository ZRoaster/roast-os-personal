package com.roastos.app

data class MachineState(

    val powerW: Int,
    val airflowPa: Int,
    val drumRpm: Int,

    val beanTemp: Double,
    val ror: Double,

    val elapsedSec: Int,

    val environmentTemp: Double,
    val environmentHumidity: Double
)

object MachineStateEngine {

    fun buildState(): MachineState {

        val telemetry = MachineTelemetryEngine.getTelemetry()

        val profile = MachineProfiles.HB_M2SE

        val power = telemetry.powerW
            .coerceIn(profile.minPowerW, profile.maxPowerW)

        val air = telemetry.airflowPa
            .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

        val rpm = telemetry.drumRpm
            .coerceIn(profile.minDrumRpm, profile.maxDrumRpm)

        return MachineState(
            powerW = power,
            airflowPa = air,
            drumRpm = rpm,
            beanTemp = telemetry.beanTemp,
            ror = telemetry.ror,
            elapsedSec = telemetry.elapsedSec,
            environmentTemp = telemetry.environmentTemp,
            environmentHumidity = telemetry.environmentHumidity
        )
    }
}
