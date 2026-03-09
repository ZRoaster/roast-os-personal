package com.roastos.app

data class MachineProfile(

    val name: String,

    // Heat system
    val maxPowerW: Int,
    val minPowerW: Int,
    val heatResponseDelaySec: Int,

    // Air system
    val maxAirflowPa: Int,
    val minAirflowPa: Int,
    val airflowResponseDelaySec: Int,

    // Drum system
    val maxDrumRpm: Int,
    val minDrumRpm: Int,

    // Thermal inertia
    val thermalInertia: Double,

    // Environmental sensitivity
    val environmentSensitivity: Double
)

object MachineProfiles {

    val HB_M2SE = MachineProfile(

        name = "HB M2SE",

        maxPowerW = 1450,
        minPowerW = 200,
        heatResponseDelaySec = 6,

        maxAirflowPa = 35,
        minAirflowPa = 3,
        airflowResponseDelaySec = 4,

        maxDrumRpm = 120,
        minDrumRpm = 40,

        thermalInertia = 0.65,

        environmentSensitivity = 0.55
    )

}
