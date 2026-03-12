package com.roastos.app

data class EnvironmentProfile(
    val altitudeMeters: Int? = null,
    val ambientTempC: Double? = null,
    val ambientHumidityRh: Double? = null,
    val barometricPressureHpa: Double? = null,
    val note: String? = null
)

data class MachineDelayProfile(
    val heatUpDelaySec: Double? = null,
    val heatDownDelaySec: Double? = null,
    val airflowDelaySec: Double? = null,
    val drumSpeedDelaySec: Double? = null,
    val coolingResponseDelaySec: Double? = null
)

data class MachineInertiaProfile(
    val thermalInertiaScore: Double? = null,
    val airflowInertiaScore: Double? = null,
    val drumInertiaScore: Double? = null
)

data class MachineCalibrationProfile(
    val calibrationId: String,
    val machineId: String,
    val machineName: String,
    val calibratedAtMillis: Long,
    val calibrationEnvironment: EnvironmentProfile,
    val delays: MachineDelayProfile,
    val inertia: MachineInertiaProfile,
    val note: String? = null
) {
    fun summary(): String {
        return """
Machine Calibration

Machine
$machineName

Calibration ID
$calibrationId

Altitude
${calibrationEnvironment.altitudeMeters ?: "-"}

Ambient Temp
${calibrationEnvironment.ambientTempC ?: "-"}

Humidity
${calibrationEnvironment.ambientHumidityRh ?: "-"}

Heat Up Delay
${delays.heatUpDelaySec ?: "-"}

Heat Down Delay
${delays.heatDownDelaySec ?: "-"}

Airflow Delay
${delays.airflowDelaySec ?: "-"}

Drum Delay
${delays.drumSpeedDelaySec ?: "-"}

Thermal Inertia
${inertia.thermalInertiaScore ?: "-"}
        """.trimIndent()
    }
}
