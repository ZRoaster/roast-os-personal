package com.roastos.app

import kotlin.math.max
import kotlin.math.sin

enum class TelemetrySourceMode {
    MANUAL,
    SIMULATOR,
    MACHINE
}

data class MachineTelemetryState(
    val mode: TelemetrySourceMode = TelemetrySourceMode.MANUAL,
    val isConnected: Boolean = false,
    val lastFrame: MachineTelemetryFrame? = null,
    val lastUpdateMillis: Long? = null,

    val liveBtC: Double? = null,
    val liveEtC: Double? = null,
    val liveRorCPerMin: Double? = null,

    val livePowerW: Int = 540,
    val liveAirflowPa: Int = 10,
    val liveDrumRpm: Int = 60,

    val liveElapsedSec: Int = 0,
    val machineState: String = "Idle"
)

object MachineTelemetryEngine {

    private var state = MachineTelemetryState()

    fun currentState(): MachineTelemetryState = state

    fun summary(): String {
        val s = state
        return """
Machine Telemetry

Mode
${s.mode}

Connected
${if (s.isConnected) "Yes" else "No"}

BT
${s.liveBtC?.let { "%.1f".format(it) + "℃" } ?: "-"}

ET
${s.liveEtC?.let { "%.1f".format(it) + "℃" } ?: "-"}

ROR
${s.liveRorCPerMin?.let { "%.1f".format(it) + "℃/min" } ?: "-"}

Power
${s.livePowerW}W

Airflow
${s.liveAirflowPa}Pa

Drum
${s.liveDrumRpm}rpm

Elapsed
${s.liveElapsedSec}s

Machine State
${s.machineState}
        """.trimIndent()
    }

    fun setMode(mode: TelemetrySourceMode) {
        state = state.copy(
            mode = mode,
            machineState = when (mode) {
                TelemetrySourceMode.MANUAL -> "Manual"
                TelemetrySourceMode.SIMULATOR -> "Simulator Ready"
                TelemetrySourceMode.MACHINE -> "Machine Selected"
            }
        )
    }

    fun reset() {
        state = MachineTelemetryState()
    }

    fun connectMachine() {
        state = state.copy(
            mode = TelemetrySourceMode.MACHINE,
            isConnected = true,
            lastUpdateMillis = System.currentTimeMillis(),
            machineState = "Connected"
        )
    }

    fun disconnectMachine() {
        state = state.copy(
            isConnected = false,
            lastUpdateMillis = System.currentTimeMillis(),
            machineState = "Disconnected"
        )
    }

    fun pushManualFrame(
        bt: Double,
        et: Double?,
        ror: Double,
        powerW: Int,
        airflowPa: Int,
        drumRpm: Int,
        elapsedSec: Int,
        machineStateLabel: String = "Manual Input",
        environmentTemp: Double = 25.0,
        environmentHumidity: Double = 50.0
    ) {
        val frame = MachineTelemetryFrame(
            machineName = "Manual Input",
            source = "MANUAL",
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
            machineStateLabel = machineStateLabel
        )

        applyFrame(
            mode = TelemetrySourceMode.MANUAL,
            frame = frame
        )
    }

    fun pushMachineFrame(
        frame: MachineTelemetryFrame
    ) {
        applyFrame(
            mode = TelemetrySourceMode.MACHINE,
            frame = frame
        )
    }

    fun pushSimulatorFrame(
        elapsedSec: Int
    ) {
        val bt = buildSimulatorBt(elapsedSec)
        val et = buildSimulatorEt(elapsedSec, bt)
        val ror = buildSimulatorRor(elapsedSec)

        val powerW = when {
            elapsedSec < 60 -> 1200
            elapsedSec < 180 -> 980
            elapsedSec < 300 -> 860
            elapsedSec < 420 -> 760
            elapsedSec < 540 -> 680
            else -> 620
        }

        val airflowPa = when {
            elapsedSec < 90 -> 6
            elapsedSec < 240 -> 8
            elapsedSec < 420 -> 10
            elapsedSec < 540 -> 12
            else -> 14
        }

        val drumRpm = 60

        val machineStateLabel = when {
            elapsedSec < 60 -> "Charging"
            elapsedSec < 240 -> "Drying"
            elapsedSec < 420 -> "Maillard"
            elapsedSec < 540 -> "Development"
            else -> "Finish Window"
        }

        val frame = MachineTelemetryFrame(
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
            environmentTemp = 25.0,
            environmentHumidity = 50.0,
            machineStateLabel = machineStateLabel
        )

        applyFrame(
            mode = TelemetrySourceMode.SIMULATOR,
            frame = frame
        )
    }

    private fun applyFrame(
        mode: TelemetrySourceMode,
        frame: MachineTelemetryFrame
    ) {
        state = state.copy(
            mode = mode,
            isConnected = frame.connectionState == TelemetryConnectionState.CONNECTED,
            lastFrame = frame,
            lastUpdateMillis = frame.timestampMillis,

            liveBtC = frame.bt,
            liveEtC = frame.et,
            liveRorCPerMin = frame.ror,

            livePowerW = frame.powerW,
            liveAirflowPa = frame.airflowPa,
            liveDrumRpm = frame.drumRpm,

            liveElapsedSec = frame.elapsedSec,
            machineState = frame.machineStateLabel
        )
    }

    private fun buildSimulatorBt(
        elapsedSec: Int
    ): Double {
        val t = elapsedSec.toDouble()

        return when {
            elapsedSec <= 0 -> 25.0
            elapsedSec < 45 -> 25.0 + t * 0.55
            elapsedSec < 180 -> 50.0 + (t - 45.0) * 0.42
            elapsedSec < 360 -> 106.7 + (t - 180.0) * 0.24
            elapsedSec < 540 -> 149.9 + (t - 360.0) * 0.15
            else -> 176.9 + (t - 540.0) * 0.08
        }
    }

    private fun buildSimulatorEt(
        elapsedSec: Int,
        bt: Double
    ): Double {
        val lift = when {
            elapsedSec < 60 -> 85.0
            elapsedSec < 180 -> 70.0
            elapsedSec < 360 -> 55.0
            elapsedSec < 540 -> 42.0
            else -> 34.0
        }

        return bt + lift + sin(elapsedSec / 35.0) * 2.5
    }

    private fun buildSimulatorRor(
        elapsedSec: Int
    ): Double {
        val base = when {
            elapsedSec < 45 -> 22.0
            elapsedSec < 120 -> 18.0
            elapsedSec < 240 -> 13.0
            elapsedSec < 360 -> 9.0
            elapsedSec < 480 -> 6.0
            elapsedSec < 600 -> 4.0
            else -> 2.5
        }

        val wave = sin(elapsedSec / 28.0) * 0.8
        return max(0.5, base + wave)
    }
}
