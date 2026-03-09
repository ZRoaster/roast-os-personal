package com.roastos.app

object DecisionEngine {

    data class DecisionResult(
        val suggestion: String,
        val severity: String,
        val reason: String
    )

    fun evaluate(
        energy: EnergySnapshot,
        stability: RoastStabilityResult,
        ror: Double
    ): DecisionResult {

        if (energy.stateEnum == EnergyState.DEFICIT) {
            return DecisionResult(
                suggestion = "Increase heat slightly",
                severity = "HIGH",
                reason = "Energy deficit detected"
            )
        }

        if (energy.stateEnum == EnergyState.LOW) {
            return DecisionResult(
                suggestion = "Increase heat",
                severity = "MEDIUM",
                reason = "Energy trending low"
            )
        }

        if (energy.stateEnum == EnergyState.HIGH && ror > 11.0) {
            return DecisionResult(
                suggestion = "Reduce heat or increase airflow",
                severity = "MEDIUM",
                reason = "Excessive energy and high ROR"
            )
        }

        if (ror < 1.0) {
            return DecisionResult(
                suggestion = "Increase heat immediately",
                severity = "HIGH",
                reason = "ROR collapse risk"
            )
        }

        if (stability.score < 40) {
            return DecisionResult(
                suggestion = "Stabilize heat input",
                severity = "MEDIUM",
                reason = "Roast stability degraded"
            )
        }

        return DecisionResult(
            suggestion = "Maintain current settings",
            severity = "LOW",
            reason = "Roast progressing normally"
        )
    }
}
