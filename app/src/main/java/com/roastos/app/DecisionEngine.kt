package com.roastos.app

object DecisionEngine {

    data class DecisionResult(
        val suggestion: String,
        val severity: String,
        val reason: String
    )

    fun evaluate(
        energy: EnergySnapshot,
        stability: RoastStabilityState,
        ror: Double
    ): DecisionResult {

        // ===== ENERGY DEFICIT =====

        if (energy.stateEnum == EnergyState.DEFICIT) {
            return DecisionResult(
                suggestion = "Increase heat slightly",
                severity = "HIGH",
                reason = "Energy deficit detected"
            )
        }

        // ===== LOW ENERGY =====

        if (energy.stateEnum == EnergyState.LOW) {
            return DecisionResult(
                suggestion = "Increase heat",
                severity = "MEDIUM",
                reason = "Energy trending low"
            )
        }

        // ===== TOO HIGH ENERGY =====

        if (energy.stateEnum == EnergyState.HIGH && ror > 11) {
            return DecisionResult(
                suggestion = "Reduce heat or increase airflow",
                severity = "MEDIUM",
                reason = "Excessive energy and high ROR"
            )
        }

        // ===== ROR COLLAPSE =====

        if (ror < 1) {
            return DecisionResult(
                suggestion = "Increase heat immediately",
                severity = "HIGH",
                reason = "ROR collapse risk"
            )
        }

        // ===== STABILITY ISSUE =====
        // 这里只检测 stability score，不用 enum

        if (stability.score < 40) {
            return DecisionResult(
                suggestion = "Stabilize heat input",
                severity = "MEDIUM",
                reason = "Roast stability degraded"
            )
        }

        // ===== GOOD STATE =====

        return DecisionResult(
            suggestion = "Maintain current settings",
            severity = "LOW",
            reason = "Roast progressing normally"
        )
    }
}
