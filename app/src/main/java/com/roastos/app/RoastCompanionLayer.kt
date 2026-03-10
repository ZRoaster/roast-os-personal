package com.roastos.app

enum class RoastCompanionMode {
    QUIET,
    SUPPORTIVE,
    EXPLORATION
}

enum class RoastCompanionPresence {
    SILENT,
    BACKGROUND,
    SURFACE,
    PROMINENT
}

enum class RoastCompanionTrigger {
    NONE,
    PERIODIC_CHECK,
    PHASE_CHANGE,
    RISK_RISING,
    USER_REQUEST,
    EXPLORATION_REQUEST
}

data class RoastCompanionVoiceLine(
    val presence: RoastCompanionPresence,
    val trigger: RoastCompanionTrigger,
    val observation: String,
    val cause: String?,
    val consequence: String?,
    val quietSummary: String,
    val primaryTitle: String?,
    val emittedAtElapsedSec: Int
) {
    fun summary(): String {
        val causeText = cause?.takeIf { it.isNotBlank() } ?: "-"
        val consequenceText = consequence?.takeIf { it.isNotBlank() } ?: "-"

        return """
Presence
$presence

Trigger
$trigger

Observation
$observation

Cause
$causeText

Consequence
$consequenceText

Quiet Summary
$quietSummary
        """.trimIndent()
    }
}

data class RoastCompanionState(
    val mode: RoastCompanionMode,
    val lastPhaseLabel: String?,
    val lastPrimaryTitle: String?,
    val lastEmitAtElapsedSec: Int,
    val lastPresence: RoastCompanionPresence
) {
    companion object {
        fun initial(
            mode: RoastCompanionMode = RoastCompanionMode.QUIET
        ): RoastCompanionState {
            return RoastCompanionState(
                mode = mode,
                lastPhaseLabel = null,
                lastPrimaryTitle = null,
                lastEmitAtElapsedSec = -9999,
                lastPresence = RoastCompanionPresence.SILENT
            )
        }
    }
}

data class RoastCompanionDecision(
    val nextState: RoastCompanionState,
    val shouldSpeak: Boolean,
    val voiceLine: RoastCompanionVoiceLine?,
    val presence: RoastCompanionPresence
)

object RoastCompanionLayer {

    fun evaluate(
        currentState: RoastCompanionState,
        report: RoastInsightReport,
        machineState: MachineState,
        userRequested: Boolean = false,
        explorationRequested: Boolean = false
    ): RoastCompanionDecision {

        val trigger = decideTrigger(
            currentState = currentState,
            report = report,
            machineState = machineState,
            userRequested = userRequested,
            explorationRequested = explorationRequested
        )

        val presence = decidePresence(
            mode = currentState.mode,
            trigger = trigger,
            report = report
        )

        val shouldSpeak = presence != RoastCompanionPresence.SILENT

        val line = if (shouldSpeak) {
            buildVoiceLine(
                trigger = trigger,
                presence = presence,
                report = report,
                machineState = machineState
            )
        } else {
            null
        }

        val nextState = RoastCompanionState(
            mode = currentState.mode,
            lastPhaseLabel = report.phaseLabel,
            lastPrimaryTitle = report.observations.firstOrNull()?.title,
            lastEmitAtElapsedSec = if (shouldSpeak) machineState.elapsedSec else currentState.lastEmitAtElapsedSec,
            lastPresence = presence
        )

        return RoastCompanionDecision(
            nextState = nextState,
            shouldSpeak = shouldSpeak,
            voiceLine = line,
            presence = presence
        )
    }

    private fun decideTrigger(
        currentState: RoastCompanionState,
        report: RoastInsightReport,
        machineState: MachineState,
        userRequested: Boolean,
        explorationRequested: Boolean
    ): RoastCompanionTrigger {
        if (userRequested) {
            return RoastCompanionTrigger.USER_REQUEST
        }

        if (explorationRequested) {
            return RoastCompanionTrigger.EXPLORATION_REQUEST
        }

        if (report.phaseLabel != currentState.lastPhaseLabel) {
            return RoastCompanionTrigger.PHASE_CHANGE
        }

        val primary = RoastInsightEngine.primaryInsight(report)
        if (primary != null && primary.severity == RoastInsightSeverity.ALERT) {
            return RoastCompanionTrigger.RISK_RISING
        }

        if (primary != null && primary.severity == RoastInsightSeverity.WATCH) {
            val elapsedSinceLast = machineState.elapsedSec - currentState.lastEmitAtElapsedSec
            if (elapsedSinceLast >= periodicIntervalSec(currentState.mode)) {
                return RoastCompanionTrigger.PERIODIC_CHECK
            }
        }

        val elapsedSinceLast = machineState.elapsedSec - currentState.lastEmitAtElapsedSec
        if (elapsedSinceLast >= periodicIntervalSec(currentState.mode)) {
            return RoastCompanionTrigger.PERIODIC_CHECK
        }

        return RoastCompanionTrigger.NONE
    }

    private fun decidePresence(
        mode: RoastCompanionMode,
        trigger: RoastCompanionTrigger,
        report: RoastInsightReport
    ): RoastCompanionPresence {
        val primary = RoastInsightEngine.primaryInsight(report)
        val severity = primary?.severity ?: RoastInsightSeverity.QUIET

        return when (trigger) {
            RoastCompanionTrigger.NONE ->
                RoastCompanionPresence.SILENT

            RoastCompanionTrigger.USER_REQUEST ->
                RoastCompanionPresence.PROMINENT

            RoastCompanionTrigger.EXPLORATION_REQUEST ->
                RoastCompanionPresence.PROMINENT

            RoastCompanionTrigger.RISK_RISING ->
                RoastCompanionPresence.PROMINENT

            RoastCompanionTrigger.PHASE_CHANGE ->
                when (mode) {
                    RoastCompanionMode.QUIET -> RoastCompanionPresence.BACKGROUND
                    RoastCompanionMode.SUPPORTIVE -> RoastCompanionPresence.SURFACE
                    RoastCompanionMode.EXPLORATION -> RoastCompanionPresence.SURFACE
                }

            RoastCompanionTrigger.PERIODIC_CHECK ->
                when (mode) {
                    RoastCompanionMode.QUIET ->
                        if (severity == RoastInsightSeverity.QUIET) {
                            RoastCompanionPresence.SILENT
                        } else {
                            RoastCompanionPresence.BACKGROUND
                        }

                    RoastCompanionMode.SUPPORTIVE ->
                        if (severity == RoastInsightSeverity.ALERT) {
                            RoastCompanionPresence.PROMINENT
                        } else {
                            RoastCompanionPresence.SURFACE
                        }

                    RoastCompanionMode.EXPLORATION ->
                        RoastCompanionPresence.SURFACE
                }
        }
    }

    private fun buildVoiceLine(
        trigger: RoastCompanionTrigger,
        presence: RoastCompanionPresence,
        report: RoastInsightReport,
        machineState: MachineState
    ): RoastCompanionVoiceLine {
        val observationItem = report.observations.firstOrNull { it.type == RoastInsightType.OBSERVATION }
            ?: report.observations.firstOrNull()

        val causeItem = report.observations.firstOrNull { it.type == RoastInsightType.CAUSE }
        val consequenceItem = report.observations.firstOrNull { it.type == RoastInsightType.CONSEQUENCE }

        val observationText = when (trigger) {
            RoastCompanionTrigger.PHASE_CHANGE ->
                "Roast has moved into ${report.phaseLabel.lowercase()}."

            RoastCompanionTrigger.RISK_RISING ->
                observationItem?.message ?: "Roast trend needs attention."

            RoastCompanionTrigger.EXPLORATION_REQUEST ->
                "This roast supports multiple possible directions."

            RoastCompanionTrigger.USER_REQUEST ->
                observationItem?.message ?: report.quietSummary

            RoastCompanionTrigger.PERIODIC_CHECK ->
                observationItem?.message ?: report.quietSummary

            RoastCompanionTrigger.NONE ->
                report.quietSummary
        }

        val causeText = when (trigger) {
            RoastCompanionTrigger.EXPLORATION_REQUEST ->
                buildExplorationCause(report)

            else ->
                causeItem?.message
        }

        val consequenceText = when (trigger) {
            RoastCompanionTrigger.EXPLORATION_REQUEST ->
                buildExplorationConsequence(report)

            else ->
                consequenceItem?.message
        }

        return RoastCompanionVoiceLine(
            presence = presence,
            trigger = trigger,
            observation = observationText,
            cause = causeText,
            consequence = consequenceText,
            quietSummary = report.quietSummary,
            primaryTitle = observationItem?.title,
            emittedAtElapsedSec = machineState.elapsedSec
        )
    }

    private fun buildExplorationCause(
        report: RoastInsightReport
    ): String? {
        val topTwo = report.possibilities.take(2)
        if (topTwo.isEmpty()) return null

        return topTwo.joinToString(" ") {
            "${it.title} is available because ${it.rationale}"
        }
    }

    private fun buildExplorationConsequence(
        report: RoastInsightReport
    ): String? {
        val topTwo = report.possibilities.take(2)
        if (topTwo.isEmpty()) return null

        return topTwo.joinToString(" ") {
            when (it.direction) {
                RoastFlavorDirection.CLARITY ->
                    "A clarity path may preserve transparency and lift."

                RoastFlavorDirection.SWEETNESS ->
                    "A sweetness path may increase roundness and caramel tone."

                RoastFlavorDirection.BODY ->
                    "A body path may deepen structure and density."

                RoastFlavorDirection.BALANCE ->
                    "A balanced path may keep the cup centered and versatile."

                RoastFlavorDirection.EXPERIMENTAL ->
                    "An experimental path may reveal a less obvious side of the coffee."
            }
        }
    }

    private fun periodicIntervalSec(
        mode: RoastCompanionMode
    ): Int {
        return when (mode) {
            RoastCompanionMode.QUIET -> 90
            RoastCompanionMode.SUPPORTIVE -> 60
            RoastCompanionMode.EXPLORATION -> 45
        }
    }

    fun renderForUi(
        decision: RoastCompanionDecision
    ): String {
        val line = decision.voiceLine ?: return "System quiet."

        val causePart = line.cause?.takeIf { it.isNotBlank() }?.let {
            "\n\nCause\n$it"
        } ?: ""

        val consequencePart = line.consequence?.takeIf { it.isNotBlank() }?.let {
            "\n\nConsequence\n$it"
        } ?: ""

        return """
Observation
${line.observation}$causePart$consequencePart
        """.trimIndent()
    }
}
