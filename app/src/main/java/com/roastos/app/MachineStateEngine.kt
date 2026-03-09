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

    fun buildState(

        powerW: Int,
        airflowPa: Int,
        drumRpm: Int,

        beanTemp: Double,
        ror: Double,

        elapsedSec: Int,

        environmentTemp: Double,
        environmentHumidity: Double

    ): MachineState {

        val profile = MachineProfiles.HB_M2SE

        val power = powerW
            .coerceIn(profile.minPowerW, profile.maxPowerW)

        val air = airflowPa
            .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

        val rpm = drumRpm
            .coerceIn(profile.minDrumRpm, profile.maxDrumRpm)

        return MachineState(
            powerW = power,
            airflowPa = air,
            drumRpm = rpm,
            beanTemp = beanTemp,
            ror = ror,
            elapsedSec = elapsedSec,
            environmentTemp = environmentTemp,
            environmentHumidity = environmentHumidity
        )
    }
}
