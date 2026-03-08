package com.roastos.app

data class DecisionOutput(
    val currentPhase: String,
    val actionNow: String,
    val heatCommand: String,
    val airCommand: String,
    val targetWindow: String,
    val riskLevel: String,
    val reason: String
)

object DecisionEngine {

    fun decide(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?,
        currentRor: Double?
    ): DecisionOutput {

        val phase = PhaseEngine.detect(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = actualTurning,
            actualYellow = actualYellow,
            actualFc = actualFc,
            actualDrop = actualDrop,
            actualPreFcRor = currentRor
        )

        val curve = CurveEngine.predict(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = actualTurning,
            actualYellow = actualYellow,
            actualFc = actualFc,
            currentRor = currentRor
        )

        if (actualDrop != null) {
            return DecisionOutput(
                currentPhase = "Finished",
                actionNow = "Batch Complete",
                heatCommand = "Heat Off / Hold",
                airCommand = "Air Safe Hold",
                targetWindow = "Move to Correction",
                riskLevel = "Low",
                reason = "Drop has already been recorded. Current batch should move into review and correction."
            )
        }

        if (actualFc != null) {
            val ror = currentRor ?: 9.0

            return when {
                ror > 10.0 -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Protect Development",
                    heatCommand = "Heat -60W",
                    airCommand = "Air +2Pa",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Pre-FC / FC energy is above target. Risk of overshoot and sharp finish is elevated."
                )

                ror < 7.0 -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Preserve Momentum",
                    heatCommand = "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Development energy is too weak. Risk of crash, hollow body, and flat finish is elevated."
                )

                else -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Hold Controlled Finish",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Development is within a manageable energy window. Focus on stable finish and target drop."
                )
            }
        }

        if (actualYellow != null) {
            val diff = actualYellow - predYellow
            val ror = currentRor ?: 13.0

            return when {
                diff > 15 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Recover Mid-Phase Momentum",
                    heatCommand = "Heat +60W",
                    airCommand = "Air Hold / Slight Delay",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow arrived late, indicating weak middle momentum. Risk of late FC and flat cup is increasing."
                )

                diff < -15 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Slow Pre-FC Push",
                    heatCommand = "Heat -60W",
                    airCommand = "Air +1Pa to +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow arrived early, indicating excessive mid-phase acceleration. Risk of pre-FC spike is elevated."
                )

                ror > 14.0 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Trim ROR",
                    heatCommand = "Heat -40W",
                    airCommand = "Air +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Current ROR is above the desired pre-FC window. System should protect against overshoot."
                )

                ror < 10.5 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Support FC Arrival",
                    heatCommand = "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Current ROR is softer than target. System should preserve enough momentum into FC."
                )

                else -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Hold Middle Phase",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Yellow timing and ROR are close to target. Maintain stable approach into FC."
                )
            }
        }

        if (actualTurning != null) {
            val diff = actualTurning - predTurning

            return when {
                diff > 8 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Recover Front-End Energy",
                    heatCommand = "Heat +60W",
                    airCommand = "Air Delay 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived late, suggesting front-end energy is weak. System should protect drying pace."
                )

                diff < -8 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Reduce Early Push",
                    heatCommand = "Heat -60W",
                    airCommand = "Air Earlier 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived early, suggesting strong front-end push. System should avoid excessive early momentum."
                )

                else -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Hold Drying Path",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Low",
                    reason = "Turning is close to target. Continue shaping drying phase toward Yellow window."
                )
            }
        }

        return DecisionOutput(
            currentPhase = phase.currentPhase,
            actionNow = "Watch First Anchor",
            heatCommand = "Heat Hold",
            airCommand = "Air Hold",
            targetWindow = "Turning ${RoastEngine.toMMSS(predTurning.toDouble())}",
            riskLevel = "Low",
            reason = "No actual event has been recorded yet. Use planner baseline and wait for Turning response."
        )
    }
}
