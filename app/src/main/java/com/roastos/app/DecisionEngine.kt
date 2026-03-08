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

        syncStateModel(
            phase = phase.currentPhase,
            currentRor = currentRor ?: defaultRorForPhase(phase.currentPhase),
            actualTurning = actualTurning,
            actualYellow = actualYellow,
            actualFc = actualFc,
            actualDrop = actualDrop,
            envTemp = envTemp,
            humidity = humidity,
            pressureKpa = pressureKpa,
            density = density,
            moisture = moisture,
            aw = aw,
            heatLevelW = heatLevelW,
            airflowPa = airflowPa,
            drumRpm = drumRpm
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

        val energy = EnergyEngine.evaluate()
        val calibration = RoastStateModel.calibration

        val calibrationSummary = buildCalibrationSummary(calibration)

        val physicsSummary = """
Energy Target ${"%.1f".format(energy.targetRor)}
Energy Pred ${"%.1f".format(energy.predictedRor)}
Energy Error ${signed1(energy.energyError)}

Net Energy ${"%.1f".format(physics.netEnergy)}
ROR+20s ${"%.1f".format(physics.predictedRor20s)}
ROR+30s ${"%.1f".format(physics.predictedRor30s)}

Bean Load ${"%.1f".format(energy.beanLoad)}
Env Load ${"%.1f".format(energy.envLoad)}
Machine Effect ${"%.1f".format(energy.machineEffect)}
Control Effect ${"%.1f".format(energy.controlEffect)}
Calibration Effect ${"%.1f".format(energy.calibrationEffect)}

Calibration
$calibrationSummary

${energy.summary}
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
                reason = "Drop has already been recorded. Current batch should move into review and correction. Calibration from previous batches is already loaded for future roasts.",
                physicsSummary = physicsSummary
            )
        }

        if (actualFc != null) {
            return decideDevelopment(
                phase = phase.currentPhase,
                curve = curve,
                currentRor = currentRor ?: 9.0,
                physics = physics,
                energy = energy,
                calibration = calibration,
                physicsSummary = physicsSummary
            )
        }

        if (actualYellow != null) {
            return decidePreFc(
                phase = phase.currentPhase,
                predYellow = predYellow,
                actualYellow = actualYellow,
                curve = curve,
                currentRor = currentRor ?: 13.0,
                physics = physics,
                energy = energy,
                calibration = calibration,
                physicsSummary = physicsSummary
            )
        }

        if (actualTurning != null) {
            return decideDrying(
                phase = phase.currentPhase,
                predTurning = predTurning,
                actualTurning = actualTurning,
                curve = curve,
                physics = physics,
                energy = energy,
                calibration = calibration,
                physicsSummary = physicsSummary
            )
        }

        return DecisionOutput(
            currentPhase = phase.currentPhase,
            actionNow = "Watch First Anchor",
            heatCommand = "Heat Hold",
            airCommand = "Air Hold",
            targetWindow = "Turning ${RoastEngine.toMMSS(predTurning.toDouble())}",
            riskLevel = "Low",
            reason = "No actual event has been recorded yet. Use planner baseline and wait for Turning response. Learned calibration is loaded but no roast anchor is available yet.",
            physicsSummary = physicsSummary
        )
    }

    private fun decideDevelopment(
        phase: String,
        curve: CurvePrediction,
        currentRor: Double,
        physics: PhysicsOutput,
        energy: EnergyState,
        calibration: RoastStateModel.CalibrationState,
        physicsSummary: String
    ): DecisionOutput {

        val strongHeatBias = calibration.heatBias > 0.3
        val weakAirBias = calibration.airBias < -0.2

        return when {
            physics.predictedRor20s > 10.5 || energy.energyError < -2.0 || currentRor > 10.0 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Protect Development",
                    heatCommand = if (strongHeatBias) "Heat -80W" else "Heat -60W",
                    airCommand = if (weakAirBias) "Air +3Pa" else "Air +2Pa",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Physics and energy layers both indicate excessive post-crack energy. Calibration also suggests this machine carries energy strongly, so trimming should be slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            physics.predictedRor20s < 6.8 || energy.energyError > 2.0 || currentRor < 7.0 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Preserve Momentum",
                    heatCommand = if (strongHeatBias) "Heat +20W" else "Heat +40W",
                    airCommand = if (weakAirBias) "Air -1Pa / Hold" else "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Physics and energy layers indicate weak development momentum. Calibration suggests command response may already be strong, so support is applied more carefully.",
                    physicsSummary = physicsSummary
                )
            }

            else -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Hold Controlled Finish",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Development energy is within a manageable window. Learned calibration does not justify extra correction beyond stable finishing.",
                    physicsSummary = physicsSummary
                )
            }
        }
    }

    private fun decidePreFc(
        phase: String,
        predYellow: Int,
        actualYellow: Int,
        curve: CurvePrediction,
        currentRor: Double,
        physics: PhysicsOutput,
        energy: EnergyState,
        calibration: RoastStateModel.CalibrationState,
        physicsSummary: String
    ): DecisionOutput {

        val yellowDelta = actualYellow - predYellow
        val strongHeatBias = calibration.heatBias > 0.3
        val strongBeanLoad = calibration.beanBias > 0.3
        val weakAirBias = calibration.airBias < -0.2

        return when {
            yellowDelta > 15 && (physics.netEnergy < 0.0 || energy.energyError > 2.0) -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Recover Mid-Phase Momentum",
                    heatCommand = if (strongBeanLoad) "Heat +80W" else "Heat +60W",
                    airCommand = "Air Hold / Slight Delay",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow is late and both physics and energy layers show insufficient momentum. Learned bean load is also high, so stronger support is justified.",
                    physicsSummary = physicsSummary
                )
            }

            yellowDelta < -15 && (physics.netEnergy > 0.8 || energy.energyError < -2.0) -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Slow Pre-FC Push",
                    heatCommand = if (strongHeatBias) "Heat -80W" else "Heat -60W",
                    airCommand = if (weakAirBias) "Air +3Pa" else "Air +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Yellow is early and both physics and energy layers still show excess momentum. Calibration suggests trimming should be slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            currentRor > 14.0 || physics.predictedRor20s > 13.5 || energy.energyError < -1.5 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Trim ROR",
                    heatCommand = if (strongHeatBias) "Heat -60W" else "Heat -40W",
                    airCommand = if (weakAirBias) "Air +3Pa" else "Air +2Pa",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "High",
                    reason = "Current and predicted ROR are above the desired pre-FC window. Learned calibration suggests this roaster may hold momentum strongly, so correction is slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            currentRor < 10.5 || physics.predictedRor20s < 10.0 || energy.energyError > 1.5 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Support FC Arrival",
                    heatCommand = if (strongBeanLoad) "Heat +60W" else "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Current and predicted ROR are softer than target. Calibration indicates bean load may be higher than baseline, so support is slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            else -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Hold Middle Phase",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Yellow timing and integrated energy state are close to target. Learned calibration does not justify extra intervention here.",
                    physicsSummary = physicsSummary
                )
            }
        }
    }

    private fun decideDrying(
        phase: String,
        predTurning: Int,
        actualTurning: Int,
        curve: CurvePrediction,
        physics: PhysicsOutput,
        energy: EnergyState,
        calibration: RoastStateModel.CalibrationState,
        physicsSummary: String
    ): DecisionOutput {

        val turningDelta = actualTurning - predTurning
        val strongBeanLoad = calibration.beanBias > 0.3
        val strongHeatBias = calibration.heatBias > 0.3
        val weakAirBias = calibration.airBias < -0.2

        return when {
            turningDelta > 8 && (physics.netEnergy < 0.0 || energy.energyError > 2.0) -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Recover Front-End Energy",
                    heatCommand = if (strongBeanLoad) "Heat +80W" else "Heat +60W",
                    airCommand = "Air Delay 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived late and integrated energy state is weak. Learned bean load suggests front-end support may need to be slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            turningDelta < -8 && (physics.netEnergy > 0.8 || energy.energyError < -2.0) -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Reduce Early Push",
                    heatCommand = if (strongHeatBias) "Heat -80W" else "Heat -60W",
                    airCommand = if (weakAirBias) "Air Earlier 10s +1Pa" else "Air Earlier 10s",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Turning arrived early and integrated energy state is still strong. Learned response suggests stronger early trimming may be appropriate.",
                    physicsSummary = physicsSummary
                )
            }

            physics.predictedRor20s > 17.5 || energy.energyError < -1.5 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Soften Drying Rise",
                    heatCommand = if (strongHeatBias) "Heat -60W" else "Heat -40W",
                    airCommand = if (weakAirBias) "Air +2Pa" else "Air +1Pa",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Integrated model predicts excessive early ROR. Learned calibration suggests slightly stronger damping is appropriate.",
                    physicsSummary = physicsSummary
                )
            }

            physics.predictedRor20s < 14.0 || energy.energyError > 1.5 -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Support Drying Pace",
                    heatCommand = if (strongBeanLoad) "Heat +60W" else "Heat +40W",
                    airCommand = "Air Hold",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Medium",
                    reason = "Integrated model predicts weak drying ROR. Learned bean load indicates support may need to be slightly stronger.",
                    physicsSummary = physicsSummary
                )
            }

            else -> {
                DecisionOutput(
                    currentPhase = phase,
                    actionNow = "Hold Drying Path",
                    heatCommand = "Heat Hold",
                    airCommand = "Air Hold",
                    targetWindow = "Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}",
                    riskLevel = "Low",
                    reason = "Turning and integrated drying energy are close to target. Learned calibration does not justify extra correction.",
                    physicsSummary = physicsSummary
                )
            }
        }
    }

    private fun syncStateModel(
        phase: String,
        currentRor: Double,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        actualDrop: Int?,
        envTemp: Double,
        humidity: Double,
        pressureKpa: Double,
        density: Double,
        moisture: Double,
        aw: Double,
        heatLevelW: Int,
        airflowPa: Int,
        drumRpm: Int
    ) {
        RoastStateModel.bean.density = density
        RoastStateModel.bean.moisture = moisture
        RoastStateModel.bean.aw = aw

        RoastStateModel.environment.ambientTemp = envTemp
        RoastStateModel.environment.ambientHumidity = humidity
        RoastStateModel.environment.ambientPressure = pressureKpa

        RoastStateModel.control.powerW = heatLevelW
        RoastStateModel.control.airflowPa = airflowPa
        RoastStateModel.control.drumRpm = drumRpm

        RoastStateModel.roast.phase = phase
        RoastStateModel.roast.ror = currentRor
        RoastStateModel.roast.turningSec = actualTurning
        RoastStateModel.roast.yellowSec = actualYellow
        RoastStateModel.roast.fcSec = actualFc
        RoastStateModel.roast.dropSec = actualDrop
    }

    private fun defaultRorForPhase(phase: String): Double {
        return when (phase) {
            "Drying" -> 16.0
            "Maillard / Pre-FC" -> 12.0
            "Development" -> 7.0
            else -> 12.0
        }
    }

    private fun signed1(value: Double): String {
        val txt = "%.1f".format(value)
        return if (value > 0) "+$txt" else txt
    }

    private fun buildCalibrationSummary(
        calibration: RoastStateModel.CalibrationState
    ): String {
        return """
FC Bias ${signed1(calibration.fcBias)}
Drop Bias ${signed1(calibration.dropBias)}
ROR Bias ${signed1(calibration.rorBias)}
Heat Bias ${signed1(calibration.heatBias)}
Air Bias ${signed1(calibration.airBias)}
Bean Bias ${signed1(calibration.beanBias)}
Machine Response ${"%.2f".format(calibration.machineResponseFactor)}
Learning Count ${calibration.learningCount}
        """.trimIndent()
    }
}
