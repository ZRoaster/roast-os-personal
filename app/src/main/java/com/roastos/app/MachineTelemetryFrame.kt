package com.roastos.app

enum class TelemetryConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class TelemetryControlMode {
    READ_ONLY,
    ASSISTED,
    FULL_CONTROL
}

data class MachineTelemetryFrame(

    val machineName: String,
    val source: String,

    val connectionState: TelemetryConnectionState,
    val controlMode: TelemetryControlMode,

    val timestampMillis: Long,

    val bt: Double,
    val et: Double?,
    val ror: Double,

    val powerW: Int,
    val airflowPa: Int,
    val drumRpm: Int,

    val elapsedSec: Int,

    val environmentTemp: Double,
    val environmentHumidity: Double,

    val machineStateLabel: String

) {

    fun summary(): String {

        return """
Machine Telemetry Frame

Machine
$machineName

Source
$source

Connection
$connectionState

Control Mode
$controlMode

Time
$elapsedSec sec

BT
$bt °C

ET
${et ?: "N/A"} °C

RoR
$ror °C/min

Power
$powerW W

Airflow
$airflowPa Pa

Drum
$drumRpm RPM

Environment
$environmentTemp °C / $environmentHumidity %

Machine State
$machineStateLabel
        """.trimIndent()
    }
}
