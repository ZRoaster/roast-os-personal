package com.roastos.app

data class PhaseState(
    val currentPhase: String,
    val nextTargetLabel: String,
    val nextTargetSec: Int,
    val phaseSummary: String,
    val riskHint: String
)

object PhaseEngine {

    fun detect(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?,
        actualPreFcRor: Double?
    ): PhaseState {

        if (actualDrop != null) {
            return PhaseState(
                currentPhase = "Finished",
                nextTargetLabel = "Batch Complete",
                nextTargetSec = actualDrop,
                phaseSummary = "Drop has been recorded. Roast batch is finished.",
                riskHint = "Review batch and move to Correction."
            )
        }

        if (actualFc != null) {
            val risk = when {
                actualPreFcRor != null && actualPreFcRor > 10.0 ->
                    "High development overshoot risk"
                actualPreFcRor != null && actualPreFcRor < 7.0 ->
                    "Development crash risk"
                else ->
                    "Moderate development risk"
            }

            return PhaseState(
                currentPhase = "Development",
                nextTargetLabel = "Drop",
                nextTargetSec = predDrop,
                phaseSummary = "FC has been recorded. You are in development phase.",
                riskHint = risk
            )
        }

        if (actualYellow != null) {
            val diff = actualYellow - predYellow

            val risk = when {
                diff > 15 -> "Late crack / flat cup risk"
                diff < -15 -> "Pre-FC overshoot risk"
                actualPreFcRor != null && actualPreFcRor > 14.0 -> "High ROR spike risk"
                else -> "Moderate Maillard-phase risk"
            }

            return PhaseState(
                currentPhase = "Maillard / Pre-FC",
                nextTargetLabel = "First Crack",
                nextTargetSec = predFc,
                phaseSummary = "Yellow has been recorded. Controlling middle momentum toward FC.",
                riskHint = risk
            )
        }

        if (actualTurning != null) {
            val diff = actualTurning - predTurning

            val risk = when {
                diff > 8 -> "Front-end energy short risk"
                diff < -8 -> "Early acceleration risk"
                else -> "Low immediate drying risk"
            }

            return PhaseState(
                currentPhase = "Drying",
                nextTargetLabel = "Yellow",
                nextTargetSec = predYellow,
                phaseSummary = "Turning has been recorded. You are shaping the drying phase.",
                riskHint = risk
            )
        }

        return PhaseState(
            currentPhase = "Pre-Turning",
            nextTargetLabel = "Turning",
            nextTargetSec = predTurning,
            phaseSummary = "No actual event recorded yet. Waiting for first anchor point.",
            riskHint = "Watch early energy and first turning response."
        )
    }
}
