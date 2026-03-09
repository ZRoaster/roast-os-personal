package com.roastos.app

enum class TelemetryConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
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

    val machineStateLabel: String,

    val isSimulated: Boolean = false
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

BT
${"%.1f".format(bt)}℃

ET
${et?.let { "%.1f".format(it) + "℃" } ?: "-"}

ROR
${"%.1f".format(ror)}℃/min

Power
${powerW}W

Airflow
${airflowPa}Pa

Drum
${drumRpm}rpm

Elapsed
${elapsedSec}s

Environment
${"%.1f".format(environmentTemp)}℃ / ${"%.0f".format(environmentHumidity)}%RH

State
$machineStateLabel

Simulated
${if (isSimulated) "Yes" else "No"}
        """.trimIndent()
    }
}

object MachineTelemetryFrames {

    fun fromMachineState(
        machineProfile: MachineProfile,
        machineState: MachineState,
        source: String = "internal",
        connectionState: TelemetryConnectionState = TelemetryConnectionState.CONNECTED,
        controlMode: TelemetryControlMode = TelemetryControlMode.READ_ONLY,
        machineStateLabel: String = "RUNNING",
        isSimulated: Boolean = false
    ): MachineTelemetryFrame {
        return MachineTelemetryFrame(
            machineName = machineProfile.name,
            source = source,
            connectionState = connectionState,
            controlMode = controlMode,
            timestampMillis = System.currentTimeMillis(),
            bt = machineState.beanTemp,
            et = null,
            ror = machineState.ror,
            powerW = machineState.powerW,
            airflowPa = machineState.airflowPa,
            drumRpm = machineState.drumRpm,
            elapsedSec = machineState.elapsedSec,
            environmentTemp = machineState.environmentTemp,
            environmentHumidity = machineState.environmentHumidity,
            machineStateLabel = machineStateLabel,
            isSimulated = isSimulated
        )
    }

    fun hbM2seSimulatorFrame(
        bt: Double,
        et: Double?,
        ror: Double,
        powerW: Int,
        airflowPa: Int,
        drumRpm: Int,
        elapsedSec: Int,
        environmentTemp: Double,
        environmentHumidity: Double
    ): MachineTelemetryFrame {
        return MachineTelemetryFrame(
            machineName = MachineProfiles.HB_M2SE.name,
            source = "HB_SIMULATOR",
            connectionState = TelemetryConnectionState.CONNECTED,
            controlMode = TelemetryControlMode.READ_ONLY,
            timestampMillis = System.currentTimeMillis(),
            bt = bt,
            et = et,
            ror = ror,
            powerW = powerW,
            airflowPa = airflowPa,
            drumRpm = drumRpm,
            elapsedSec = elapsedSec,
            environmentTemp = environmentTemp,
            environmentHumidity = environmentHumidity,
            machineStateLabel = "SIMULATING",
            isSimulated = true
        )
    }

    fun empty(
        machineName: String = "Unknown Machine",
        source: String = "unknown"
    ): MachineTelemetryFrame {
        return MachineTelemetryFrame(
            machineName = machineName,
            source = source,
            connectionState = TelemetryConnectionState.DISCONNECTED,
            controlMode = TelemetryControlMode.READ_ONLY,
            timestampMillis = System.currentTimeMillis(),
            bt = 0.0,
            et = null,
            ror = 0.0,
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            elapsedSec = 0,
            environmentTemp = 25.0,
            environmentHumidity = 50.0,
            machineStateLabel = "IDLE",
            isSimulated = false
        )
    }
}
