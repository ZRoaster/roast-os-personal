package com.roastos.app

data class RoastPhaseEvent(
    val key: String,
    val label: String,
    val elapsedSec: Int,
    val beanTemp: Double
)

data class RoastPhaseDetectionState(
    val turningPoint: RoastPhaseEvent? = null,
    val dryEnd: RoastPhaseEvent? = null,
    val maillardStart: RoastPhaseEvent? = null,
    val firstCrack: RoastPhaseEvent? = null,
    val drop: RoastPhaseEvent? = null
)

object RoastPhaseDetectionEngine {

    private var state = RoastPhaseDetectionState()
    private var lowestBeanTempAfterCharge: Double? = null

    fun reset() {
        state = RoastPhaseDetectionState()
        lowestBeanTempAfterCharge = null
    }

    fun currentState(): RoastPhaseDetectionState {
        return state
    }

    fun update(session: RoastSessionState): RoastPhaseDetectionState {
        val bt = session.lastBeanTemp
        val elapsed = session.lastElapsedSec

        if (session.status != RoastSessionStatus.RUNNING) {
            return state
        }

        if (elapsed <= 90) {
            lowestBeanTempAfterCharge =
                when (val lowest = lowestBeanTempAfterCharge) {
                    null -> bt
                    else -> minOf(lowest, bt)
                }
        }

        if (state.turningPoint == null &&
            lowestBeanTempAfterCharge != null &&
            bt >= lowestBeanTempAfterCharge!! + 1.5 &&
            elapsed > 20
        ) {
            state = state.copy(
                turningPoint = RoastPhaseEvent(
                    key = "turning_point",
                    label = "Turning Point",
                    elapsedSec = elapsed,
                    beanTemp = bt
                )
            )
        }

        if (state.dryEnd == null && bt >= 150.0) {
            state = state.copy(
                dryEnd = RoastPhaseEvent(
                    key = "dry_end",
                    label = "Dry End",
                    elapsedSec = elapsed,
                    beanTemp = bt
                )
            )
        }

        if (state.maillardStart == null && bt >= 160.0) {
            state = state.copy(
                maillardStart = RoastPhaseEvent(
                    key = "maillard_start",
                    label = "Maillard Start",
                    elapsedSec = elapsed,
                    beanTemp = bt
                )
            )
        }

        if (state.firstCrack == null && session.firstCrackLikely) {
            state = state.copy(
                firstCrack = RoastPhaseEvent(
                    key = "first_crack",
                    label = "First Crack",
                    elapsedSec = elapsed,
                    beanTemp = bt
                )
            )
        }

        if (state.drop == null && session.dropSuggested) {
            state = state.copy(
                drop = RoastPhaseEvent(
                    key = "drop",
                    label = "Drop",
                    elapsedSec = elapsed,
                    beanTemp = bt
                )
            )
        }

        return state
    }

    fun summary(): String {
        val s = state

        return buildString {
            appendLine(formatEvent("Turning Point", s.turningPoint))
            appendLine(formatEvent("Dry End", s.dryEnd))
            appendLine(formatEvent("Maillard Start", s.maillardStart))
            appendLine(formatEvent("First Crack", s.firstCrack))
            append(formatEvent("Drop", s.drop))
        }
    }

    private fun formatEvent(
        label: String,
        event: RoastPhaseEvent?
    ): String {
        return if (event == null) {
            "$label\n-"
        } else {
            "$label\n${event.elapsedSec}s · ${"%.1f".format(event.beanTemp)}℃"
        }
    }
}
