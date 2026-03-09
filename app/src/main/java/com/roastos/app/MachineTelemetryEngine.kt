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

    fun currentState(): MachineTelemetryState {
        return state
    }

    fun setMode(mode: TelemetrySourceMode) {
        state = state.copy(mode = mode)
    }

    fun connectMachine() {
        state = state.copy(
            mode = TelemetrySourceMode.MACHINE,
            isConnected = true
        )
    }

    fun disconnectMachine() {
        state = state.copy(
            isConnected = false,
            mode = TelemetrySourceMode.MANUAL
        )
    }

    fun reset() {
        state = MachineTelemetryState(
            mode = state.mode,
            isConnected = state.isConnected
        )
    }

    fun pushManualFrame(
        btC: Double?,
        etC: Double?,
        rorCPerMin: Double?,
        powerW: Int?,
        airflowPa: Int?,
        drumRpm: Int?,
        elapsedSec: Int?,
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?,
        machineState: String = "Running"
    ): MachineTelemetryFrame {
        val frame = MachineTelemetryFrame(
            btC = btC,
            etC = etC,
            rorCPerMin = rorCPerMin,
            powerW = powerW,
            airflowPa = airflowPa,
            drumRpm = drumRpm,
            elapsedSec = elapsedSec,
            turningSec = turningSec,
            yellowSec = yellowSec,
            fcSec = fcSec,
            dropSec = dropSec,
            machineState = machineState,
            sourceLabel = "manual"
        )

        acceptFrame(frame, TelemetrySourceMode.MANUAL)
        return frame
    }

    fun pushMachineFrame(frame: MachineTelemetryFrame) {
        acceptFrame(
            frame = frame.copy(sourceLabel = "machine"),
            sourceOverride = TelemetrySourceMode.MACHINE
        )
    }

    fun buildSimulatorFrame(elapsedSec: Int): MachineTelemetryFrame {
        val t = max(0, elapsedSec)

        val bt = when {
            t <= 70 -> 200.0 - t * 1.45
            t <= 240 -> 98.5 + (t - 70) * 0.30
            t <= 480 -> 149.5 + (t - 240) * 0.19
            else -> 195.1 + (t - 480) * 0.11
        }

        val et = bt + 18.0 + 2.0 * sin(t / 28.0)

        val ror = when {
            t <= 70 -> -8.0
            t <= 180 -> 13.0
            t <= 300 -> 11.0
            t <= 420 -> 9.0
            t <= 520 -> 7.8
            else -> 6.2
        }

        val power = when {
            t <= 60 -> 620
            t <= 180 -> 600
            t <= 300 -> 570
            t <= 430 -> 540
            t <= 520 -> 500
            else -> 460
        }

        val air = when {
            t <= 120 -> 8
            t <= 260 -> 10
            t <= 430 -> 12
            else -> 14
        }

        val drum = 60

        val turning = if (t >= 70) 70 else null
        val yellow = if (t >= 240) 240 else null
        val fc = if (t >= 480) 480 else null
        val drop = if (t >= 570) 570 else null

        return MachineTelemetryFrame(
            btC = bt,
            etC = et,
            rorCPerMin = ror,
            powerW = power,
            airflowPa = air,
            drumRpm = drum,
            elapsedSec = t,
            turningSec = turning,
            yellowSec = yellow,
            fcSec = fc,
            dropSec = drop,
            machineState = if (drop != null) "Finished" else "Running",
            sourceLabel = "simulator"
        )
    }

    fun pushSimulatorFrame(elapsedSec: Int): MachineTelemetryFrame {
        val frame = buildSimulatorFrame(elapsedSec)
        acceptFrame(frame, TelemetrySourceMode.SIMULATOR)
        return frame
    }

    fun summary(): String {
        val s = state
        val f = s.lastFrame

        return """
Machine Telemetry

Mode
${s.mode}

Connected
${if (s.isConnected) "Yes" else "No"}

Machine State
${s.machineState}

Elapsed
${s.liveElapsedSec}s

BT
${s.liveBtC?.let { "%.1f".format(it) + "℃" } ?: "-"}

ET
${s.liveEtC?.let { "%.1f".format(it) + "℃" } ?: "-"}

ROR
${s.liveRorCPerMin?.let { "%.1f".format(it) + "℃/min" } ?: "-"}

Power
${s.livePowerW}W

Air
${s.liveAirflowPa}Pa

Drum
${s.liveDrumRpm}rpm

Last Source
${f?.sourceLabel ?: "-"}
        """.trimIndent()
    }

    private fun acceptFrame(
        frame: MachineTelemetryFrame,
        sourceOverride: TelemetrySourceMode
    ) {
        val now = System.currentTimeMillis()

        val nextState = state.copy(
            mode = sourceOverride,
            lastFrame = frame,
            lastUpdateMillis = now,
            liveBtC = frame.btC ?: state.liveBtC,
            liveEtC = frame.etC ?: state.liveEtC,
            liveRorCPerMin = frame.rorCPerMin ?: state.liveRorCPerMin,
            livePowerW = frame.powerW ?: state.livePowerW,
            liveAirflowPa = frame.airflowPa ?: state.liveAirflowPa,
            liveDrumRpm = frame.drumRpm ?: state.liveDrumRpm,
            liveElapsedSec = frame.elapsedSec ?: state.liveElapsedSec,
            machineState = frame.machineState
        )

        state = nextState

        syncIntoAppState(nextState, frame)
        syncIntoRoastStateModel(nextState, frame)
        syncIntoTimeline(frame)
    }

    private fun syncIntoAppState(
        telemetry: MachineTelemetryState,
        frame: MachineTelemetryFrame
    ) {
        AppState.liveActualTurningSec = frame.turningSec ?: AppState.liveActualTurningSec
        AppState.liveActualYellowSec = frame.yellowSec ?: AppState.liveActualYellowSec
        AppState.liveActualFcSec = frame.fcSec ?: AppState.liveActualFcSec
        AppState.liveActualDropSec = frame.dropSec ?: AppState.liveActualDropSec
        AppState.liveActualPreFcRor = telemetry.liveRorCPerMin ?: AppState.liveActualPreFcRor
    }

    private fun syncIntoRoastStateModel(
        telemetry: MachineTelemetryState,
        frame: MachineTelemetryFrame
    ) {
        val phase = when {
            frame.dropSec != null -> "Finished"
            frame.fcSec != null -> "Development"
            frame.yellowSec != null -> "Maillard / Pre-FC"
            frame.turningSec != null -> "Drying"
            telemetry.machineState.equals("Running", ignoreCase = true) -> "Running"
            else -> "Idle"
        }

        RoastStateModel.syncLiveState(
            phase = phase,
            ror = telemetry.liveRorCPerMin ?: 0.0,
            turningSec = frame.turningSec ?: AppState.liveActualTurningSec,
            yellowSec = frame.yellowSec ?: AppState.liveActualYellowSec,
            fcSec = frame.fcSec ?: AppState.liveActualFcSec,
            dropSec = frame.dropSec ?: AppState.liveActualDropSec,
            powerW = telemetry.livePowerW,
            airflowPa = telemetry.liveAirflowPa,
            drumRpm = telemetry.liveDrumRpm
        )
    }

    private fun syncIntoTimeline(frame: MachineTelemetryFrame) {
        RoastTimelineStore.syncActual(
            turningSec = frame.turningSec ?: AppState.liveActualTurningSec,
            yellowSec = frame.yellowSec ?: AppState.liveActualYellowSec,
            fcSec = frame.fcSec ?: AppState.liveActualFcSec,
            dropSec = frame.dropSec ?: AppState.liveActualDropSec,
            ror = frame.rorCPerMin ?: AppState.liveActualPreFcRor
        )
    }
}
