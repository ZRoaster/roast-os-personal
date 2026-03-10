package com.roastos.app

enum class RoastSessionPhase {
    IDLE,
    PREHEAT,
    CHARGE,
    TURNING,
    DRYING,
    MAILLARD,
    FIRST_CRACK_WINDOW,
    DEVELOPMENT,
    DROP,
    COOLING,
    END
}

enum class RoastSessionStatus {
    STOPPED,
    READY,
    RUNNING,
    FINISHED
}

data class RoastSessionSnapshot(
    val status: RoastSessionStatus,
    val phase: RoastSessionPhase,
    val batchActive: Boolean,
    val sessionId: String?,
    val startedAtMs: Long?,
    val finishedAtMs: Long?,
    val elapsedSec: Int,
    val beanTemp: Double,
    val ror: Double,
    val powerW: Int,
    val airflowPa: Int,
    val drumRpm: Int,
    val chargeDetected: Boolean,
    val firstCrackLikely: Boolean,
    val dropSuggested: Boolean,
    val phaseLabel: String,
    val summary: String
)

data class RoastSessionState(
    val status: RoastSessionStatus = RoastSessionStatus.STOPPED,
    val phase: RoastSessionPhase = RoastSessionPhase.IDLE,
    val sessionId: String? = null,
    val startedAtMs: Long? = null,
    val finishedAtMs: Long? = null,
    val chargeDetected: Boolean = false,
    val firstCrackLikely: Boolean = false,
    val dropSuggested: Boolean = false,
    val lastBeanTemp: Double = 0.0,
    val lastRor: Double = 0.0,
    val lastElapsedSec: Int = 0
)

object RoastSessionEngine {

    private var state = RoastSessionState()

    fun currentState(): RoastSessionState {
        return state
    }

    fun reset() {
        state = RoastSessionState()
    }

    fun markReady() {
        state = state.copy(
            status = RoastSessionStatus.READY,
            phase = RoastSessionPhase.PREHEAT,
            sessionId = null,
            startedAtMs = null,
            finishedAtMs = null,
            chargeDetected = false,
            firstCrackLikely = false,
            dropSuggested = false,
            lastBeanTemp = 0.0,
            lastRor = 0.0,
            lastElapsedSec = 0
        )
    }

    fun startSession(nowMs: Long = System.currentTimeMillis()) {
        state = state.copy(
            status = RoastSessionStatus.RUNNING,
            phase = RoastSessionPhase.CHARGE,
            sessionId = buildSessionId(nowMs),
            startedAtMs = nowMs,
            finishedAtMs = null,
            chargeDetected = true,
            firstCrackLikely = false,
            dropSuggested = false,
            lastElapsedSec = 0
        )
    }

    fun finishSession(nowMs: Long = System.currentTimeMillis()) {
        state = state.copy(
            status = RoastSessionStatus.FINISHED,
            phase = RoastSessionPhase.END,
            finishedAtMs = nowMs
        )
    }

    fun update(
        machineState: MachineState,
        nowMs: Long = System.currentTimeMillis()
    ): RoastSessionSnapshot {

        val nextStatus = decideStatus(
            previous = state,
            machineState = machineState
        )

        val nextPhase = decidePhase(
            previous = state,
            nextStatus = nextStatus,
            machineState = machineState
        )

        val chargeDetected = state.chargeDetected || nextPhase != RoastSessionPhase.IDLE
        val firstCrackLikely = state.firstCrackLikely || detectFirstCrackLikely(machineState)
        val dropSuggested = detectDropSuggested(machineState, nextPhase, firstCrackLikely)

        val nextSessionId =
            when {
                nextStatus == RoastSessionStatus.RUNNING && state.sessionId == null ->
                    buildSessionId(nowMs)

                else -> state.sessionId
            }

        val nextStartedAt =
            when {
                nextStatus == RoastSessionStatus.RUNNING && state.startedAtMs == null ->
                    nowMs

                else -> state.startedAtMs
            }

        val nextFinishedAt =
            when {
                nextStatus == RoastSessionStatus.FINISHED ->
                    nowMs

                else -> state.finishedAtMs
            }

        state = state.copy(
            status = nextStatus,
            phase = nextPhase,
            sessionId = nextSessionId,
            startedAtMs = nextStartedAt,
            finishedAtMs = nextFinishedAt,
            chargeDetected = chargeDetected,
            firstCrackLikely = firstCrackLikely,
            dropSuggested = dropSuggested,
            lastBeanTemp = machineState.beanTemp,
            lastRor = machineState.ror,
            lastElapsedSec = machineState.elapsedSec
        )

        return snapshotFromState(
            state = state,
            machineState = machineState
        )
    }

    fun snapshot(
        machineState: MachineState
    ): RoastSessionSnapshot {
        return snapshotFromState(
            state = state,
            machineState = machineState
        )
    }

    private fun decideStatus(
        previous: RoastSessionState,
        machineState: MachineState
    ): RoastSessionStatus {
        if (previous.status == RoastSessionStatus.FINISHED) {
            return RoastSessionStatus.FINISHED
        }

        val bt = machineState.beanTemp
        val power = machineState.powerW
        val elapsed = machineState.elapsedSec

        return when {
            elapsed > 0 || bt > 35.0 -> RoastSessionStatus.RUNNING
            power > 0 -> RoastSessionStatus.READY
            else -> RoastSessionStatus.STOPPED
        }
    }

    private fun decidePhase(
        previous: RoastSessionState,
        nextStatus: RoastSessionStatus,
        machineState: MachineState
    ): RoastSessionPhase {
        if (nextStatus == RoastSessionStatus.STOPPED) {
            return RoastSessionPhase.IDLE
        }

        if (nextStatus == RoastSessionStatus.READY) {
            return RoastSessionPhase.PREHEAT
        }

        if (nextStatus == RoastSessionStatus.FINISHED) {
            return RoastSessionPhase.END
        }

        val bt = machineState.beanTemp
        val ror = machineState.ror
        val elapsed = machineState.elapsedSec
        val prevPhase = previous.phase

        return when {
            elapsed <= 20 -> RoastSessionPhase.CHARGE
            bt < 60.0 -> RoastSessionPhase.TURNING
            bt < 150.0 -> RoastSessionPhase.DRYING
            bt < 185.0 -> RoastSessionPhase.MAILLARD
            bt < 196.0 -> RoastSessionPhase.FIRST_CRACK_WINDOW
            bt < 208.0 -> RoastSessionPhase.DEVELOPMENT
            bt < 220.0 && ror > 1.0 -> RoastSessionPhase.DROP
            prevPhase == RoastSessionPhase.DROP && ror <= 1.0 -> RoastSessionPhase.COOLING
            else -> RoastSessionPhase.COOLING
        }
    }

    private fun detectFirstCrackLikely(
        machineState: MachineState
    ): Boolean {
        val bt = machineState.beanTemp
        val ror = machineState.ror

        return bt >= 188.0 && ror in 2.0..12.0
    }

    private fun detectDropSuggested(
        machineState: MachineState,
        phase: RoastSessionPhase,
        firstCrackLikely: Boolean
    ): Boolean {
        val bt = machineState.beanTemp
        val ror = machineState.ror

        return when {
            phase == RoastSessionPhase.DEVELOPMENT && firstCrackLikely && bt >= 205.0 -> true
            phase == RoastSessionPhase.DROP -> true
            bt >= 210.0 && ror <= 3.0 -> true
            else -> false
        }
    }

    private fun snapshotFromState(
        state: RoastSessionState,
        machineState: MachineState
    ): RoastSessionSnapshot {
        val phaseLabel = phaseLabel(state.phase)

        return RoastSessionSnapshot(
            status = state.status,
            phase = state.phase,
            batchActive = state.status == RoastSessionStatus.RUNNING,
            sessionId = state.sessionId,
            startedAtMs = state.startedAtMs,
            finishedAtMs = state.finishedAtMs,
            elapsedSec = machineState.elapsedSec,
            beanTemp = machineState.beanTemp,
            ror = machineState.ror,
            powerW = machineState.powerW,
            airflowPa = machineState.airflowPa,
            drumRpm = machineState.drumRpm,
            chargeDetected = state.chargeDetected,
            firstCrackLikely = state.firstCrackLikely,
            dropSuggested = state.dropSuggested,
            phaseLabel = phaseLabel,
            summary = buildSummary(
                status = state.status,
                phaseLabel = phaseLabel,
                machineState = machineState
            )
        )
    }

    fun phaseLabel(
        phase: RoastSessionPhase
    ): String {
        return when (phase) {
            RoastSessionPhase.IDLE -> "Idle"
            RoastSessionPhase.PREHEAT -> "Preheat"
            RoastSessionPhase.CHARGE -> "Charge"
            RoastSessionPhase.TURNING -> "Turning"
            RoastSessionPhase.DRYING -> "Drying"
            RoastSessionPhase.MAILLARD -> "Maillard"
            RoastSessionPhase.FIRST_CRACK_WINDOW -> "First Crack Window"
            RoastSessionPhase.DEVELOPMENT -> "Development"
            RoastSessionPhase.DROP -> "Drop"
            RoastSessionPhase.COOLING -> "Cooling"
            RoastSessionPhase.END -> "End"
        }
    }

    private fun buildSummary(
        status: RoastSessionStatus,
        phaseLabel: String,
        machineState: MachineState
    ): String {
        return buildString {
            append(status.name)
            append(" · ")
            append(phaseLabel)
            append(" · ")
            append("BT ")
            append("%.1f".format(machineState.beanTemp))
            append("℃")
            append(" · ")
            append("RoR ")
            append("%.1f".format(machineState.ror))
            append("℃/min")
        }
    }

    private fun buildSessionId(
        nowMs: Long
    ): String {
        return "roast-$nowMs"
    }
}
