package com.roastos.app

data class RoastLiveAssistSnapshot(
    val phase: String,
    val risk: String,
    val actionNow: String,
    val nextWatchpoint: String,
    val interpretation: String,
    val summary: String
)

object RoastLiveAssistEngine {

    fun buildFromTelemetry(): RoastLiveAssistSnapshot {
        val telemetry = MachineTelemetryEngine.currentState()

        val bt = telemetry.liveBtC ?: 0.0
        val ror = telemetry.liveRorCPerMin ?: 0.0
        val elapsedSec = telemetry.liveElapsedSec

        return build(
            bt = bt,
            ror = ror,
            elapsedSec = elapsedSec
        )
    }

    fun build(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): RoastLiveAssistSnapshot {
        val phase = buildPhase(
            bt = bt,
            elapsedSec = elapsedSec
        )

        val risk = buildRisk(
            ror = ror,
            elapsedSec = elapsedSec
        )

        val actionNow = buildActionNow(
            bt = bt,
            ror = ror,
            elapsedSec = elapsedSec
        )

        val nextWatchpoint = buildNextWatchpoint(
            bt = bt,
            ror = ror,
            elapsedSec = elapsedSec
        )

        val interpretation = buildInterpretation(
            bt = bt,
            ror = ror,
            elapsedSec = elapsedSec
        )

        val summary = """
PHASE
$phase

RISK
$risk

ACTION NOW
$actionNow

NEXT WATCHPOINT
$nextWatchpoint

INTERPRETATION
$interpretation
        """.trimIndent()

        return RoastLiveAssistSnapshot(
            phase = phase,
            risk = risk,
            actionNow = actionNow,
            nextWatchpoint = nextWatchpoint,
            interpretation = interpretation,
            summary = summary
        )
    }

    private fun buildPhase(
        bt: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Charge / Early Front-End"
            bt <= 120.0 -> "Drying"
            bt <= 160.0 -> "Drying → Maillard Transition"
            bt <= 195.0 -> "Maillard / Pre-FC"
            else -> "Development / Late Roast"
        }
    }

    private fun buildRisk(
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Low"
            ror >= 10.8 -> "High"
            ror <= 7.0 && elapsedSec >= 240 -> "High"
            ror >= 9.5 || (ror <= 8.0 && elapsedSec >= 180) -> "Medium"
            else -> "Low"
        }
    }

    private fun buildActionNow(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 ->
                "Watch early momentum and avoid over-reacting too fast"

            ror >= 10.8 ->
                "Reduce heat earlier and watch late acceleration"

            ror <= 7.0 && elapsedSec >= 240 ->
                "Protect energy immediately and avoid crash into crack"

            bt <= 120.0 ->
                "Maintain stable drying and avoid unnecessary aggressive changes"

            bt <= 160.0 ->
                "Guide transition cleanly and keep momentum into Maillard"

            bt <= 195.0 ->
                "Monitor ROR carefully and prepare first-crack entry structure"

            else ->
                "Control development and prepare disciplined drop timing"
        }
    }

    private fun buildNextWatchpoint(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 ->
                "First turning response and early front-end strength"

            bt <= 120.0 ->
                "Drying completion and momentum into Yellow"

            bt <= 160.0 ->
                "Yellow timing and middle-phase energy continuity"

            bt <= 195.0 && ror >= 10.0 ->
                "Spike risk before first crack"

            bt <= 195.0 ->
                "FC approach timing and pre-FC ROR stability"

            else ->
                "Development time, drop point, and finish cleanliness"
        }
    }

    private fun buildInterpretation(
        bt: Double,
        ror: Double,
        elapsedSec: Int
    ): String {
        return when {
            elapsedSec <= 60 -> "Front-end phase, monitor early momentum"
            ror >= 10.8 -> "Late acceleration risk is high"
            ror <= 7.0 && elapsedSec >= 240 -> "Energy may be collapsing"
            ror in 8.0..9.8 -> "ROR looks relatively stable"
            bt <= 120.0 -> "Likely early drying stage"
            bt <= 160.0 -> "Likely drying to Maillard transition"
            bt <= 195.0 -> "Likely Maillard / pre-FC stage"
            else -> "Likely development stage"
        }
    }
}
