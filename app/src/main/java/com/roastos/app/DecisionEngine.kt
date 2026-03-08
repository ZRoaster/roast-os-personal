package com.roastos.app

data class DecisionOutput(
    val currentPhase: String,
    val actionNow: String,
    val heatCommand: String,
    val airCommand: String,
    val targetWindow: String,
    val riskLevel: String,
    val reason: String,
    val physicsSummary: String
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
        currentRor: Double?,
        envTemp: Double,
        humidity: Double,
        pressureKpa: Double,
        density: Double,
        moisture: Double,
        aw: Double,
        heatLevelW: Int,
        airflowPa: Int,
        drumRpm: Int
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

        val physics = RoastPhysicsEngine.simulate(
            PhysicsInput(
                phase = phase.currentPhase,
                currentRor = currentRor ?: defaultRorForPhase(phase.currentPhase),
                heatLevelW = heatLevelW,
                airflowPa = airflowPa,
                drumRpm = drumRpm,
                envTemp = envTemp,
                humidity = humidity,
                pressureKpa = pressureKpa,
                density = density,
                moisture = moisture,
                aw = aw
            )
        )

        val physicsSummary = """
Net Energy ${"%.1f".format(physics.netEnergy)}
ROR+20s ${"%.1f".format(physics.predictedRor20s)}
ROR+30s ${"%.1f".format(physics.predictedRor30s)}
${physics.summary}
        """.trimIndent()

        if (actualDrop != null) {
            return DecisionOutput(
                currentPhase = "Finished",
                actionNow = "Batch Complete",
                heatCommand = "Heat Off / Hold",
                airCommand = "Air Safe Hold",
                targetWindow = "Move to Correction",
                riskLevel = "Low",
                reason = "Drop has already been recorded. Current batch should move into review and correction.",
                physicsSummary = physicsSummary
            )
        }

        if (actualFc != null) {
            val ror = currentRor ?: 9.0

            return when {
                physics.predictedRor20s > 10.5 || ror > 10.0 -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Protect Development",
                    heatCommand = "Heat -60W",
                    airCommand = "Air +2Pa",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Physics layer predicts excessive development energy. Risk of overshoot and sharp finish is elevated.",
                    physicsSummary = physicsSummary
                )

                physics.predictedRor20s < 6.8 || ror < 7.0 -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Preserve Momentum",
                    heatCommand = "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Physics layer predicts weak development energy. Risk of crash, hollow body, and flat finish is elevated.",
                    physicsSummary = physicsSummary
                )

                else -> DecisionOutput(
                    currentPhase = "Development",
                    actionNow = "Hold Controlled Finish",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Development energy is within a manageable window. Focus on stable finish and target drop.",
                    physicsSummary = physicsSummary
                )
            }
        }

        if (actualYellow != null) {
            val diff = actualYellow - predYellow
            val ror = currentRor ?: 13.0

            return when {
                diff > 15 && physics.netEnergy < 0.0 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Recover Mid-Phase Momentum",
                    heatCommand = "Heat +60W",
                    airCommand = "Air Hold / Slight Delay",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow is late and physics layer shows weak net energy. Risk of late FC and flat cup is increasing.",
                    physicsSummary = physicsSummary
                )

                diff < -15 && physics.netEnergy > 0.8 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Slow Pre-FC Push",
                    heatCommand = "Heat -60W",
                    airCommand = "Air +1Pa to +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow is early and physics layer still shows positive net energy. Risk of pre-FC spike is elevated.",
                    physicsSummary = physicsSummary
                )

                ror > 14.0 || physics.predictedRor20s > 13.5 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Trim ROR",
                    heatCommand = "Heat -40W",
                    airCommand = "Air +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Current and predicted ROR are above the desired pre-FC window. System should protect against overshoot.",
                    physicsSummary = physicsSummary
                )

                ror < 10.5 || physics.predictedRor20s < 10.0 -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Support FC Arrival",
                    heatCommand = "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Current and predicted ROR are softer than target. System should preserve enough momentum into FC.",
                    physicsSummary = physicsSummary
                )

                else -> DecisionOutput(
                    currentPhase = "Maillard / Pre-FC",
                    actionNow = "Hold Middle Phase",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Yellow timing and physics-based energy state are close to target. Maintain stable approach into FC.",
                    physicsSummary = physicsSummary
                )
            }
        }

        if (actualTurning != null) {
            val diff = actualTurning - predTurning

            return when {
                diff > 8 && physics.netEnergy < 0.0 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Recover Front-End Energy",
                    heatCommand = "Heat +60W",
                    airCommand = "Air Delay 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived late and physics layer shows weak net energy. System should protect drying pace.",
                    physicsSummary = physicsSummary
                )

                diff < -8 && physics.netEnergy > 0.8 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Reduce Early Push",
                    heatCommand = "Heat -60W",
                    airCommand = "Air Earlier 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived early and physics layer still shows strong net energy. Avoid excessive early momentum.",
                    physicsSummary = physicsSummary
                )

                physics.predictedRor20s > 17.5 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Soften Drying Rise",
                    heatCommand = "Heat -40W",
                    airCommand = "Air +1Pa",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Physics layer predicts excessive early ROR. System should avoid over-compressing drying.",
                    physicsSummary = physicsSummary
                )

                physics.predictedRor20s < 14.0 -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Support Drying Pace",
                    heatCommand = "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Physics layer predicts weak drying ROR. System should support front-end pace.",
                    physicsSummary = physicsSummary
                )

                else -> DecisionOutput(
                    currentPhase = "Drying",
                    actionNow = "Hold Drying Path",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Low",
                    reason = "Turning and physics-based drying energy are close to target. Continue shaping drying phase.",
                    physicsSummary = physicsSummary
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
            reason = "No actual event has been recorded yet. Use planner baseline and wait for Turning response.",
            physicsSummary = physicsSummary
        )
    }

    private fun defaultRorForPhase(phase: String): Double {
        return when (phase) {
            "Drying" -> 16.0
            "Maillard / Pre-FC" -> 12.0
            "Development" -> 7.0
            else -> 12.0
        }
    }
}
