package com.roastos.app

enum class RoastAiPhase {
    PREHEAT,
    CHARGE,
    TURNING,
    DRYING,
    MAILLARD,
    FIRST_CRACK_WINDOW,
    DEVELOPMENT,
    DROP_WINDOW,
    FINISHED,
    UNKNOWN
}

enum class RoastAiTrend {
    FALLING,
    SOFT,
    STABLE,
    RISING,
    AGGRESSIVE
}

data class RoastAiBrainState(
    val machineName: String,
    val phase: RoastAiPhase,
    val phaseReason: String,

    val energyState: String,
    val energyTrend: RoastAiTrend,

    val stabilityState: String,
    val stabilityRisk: RoastAiRiskLevel,

    val rorTrend: RoastAiTrend,
    val heatPressure: String,
    val airflowPressure: String,

    val controlLevel: MachineControlLevel,
    val canActDirectly: Boolean,

    val operatorSummary: String,
    val aiSummary: String
) {
    fun summary(): String {
        return """
Roast AI Brain State

Machine
$machineName

Phase
$phase

Phase Reason
$phaseReason

Energy State
$energyState

Energy Trend
$energyTrend

Stability State
$stabilityState

Stability Risk
$stabilityRisk

ROR Trend
$rorTrend

Heat Pressure
$heatPressure

Airflow Pressure
$airflowPressure

Control Level
$controlLevel

Can Act Directly
${if (canActDirectly) "Yes" else "No"}

Operator Summary
$operatorSummary

AI Summary
$aiSummary
        """.trimIndent()
    }
}

object RoastAiBrain {

    fun build(
        profile: MachineProfile,
        capability: MachineControlCapability,
        machineState: MachineState,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        styleGoal: RoastAiStyleGoal? = null
    ): RoastAiBrainState {

        val phase = inferPhase(machineState)
        val phaseReason = buildPhaseReason(machineState, phase)

        val energyState = energy?.energyState ?: "Unknown Energy"
        val energyTrend = inferEnergyTrend(energy)

        val stabilityState = stability?.stability ?: "Unknown Stability"
        val stabilityRisk = inferStabilityRisk(stability)

        val rorTrend = inferRorTrend(machineState.ror)
        val heatPressure = inferHeatPressure(profile, machineState)
        val airflowPressure = inferAirflowPressure(profile, machineState)

        val canActDirectly =
            capability.controlLevel == MachineControlLevel.FULL_CONTROL &&
                capability.canAutoExecute

        val operatorSummary = buildOperatorSummary(
            phase = phase,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = styleGoal
        )

        val aiSummary = buildAiSummary(
            phase = phase,
            energyState = energyState,
            energyTrend = energyTrend,
            stabilityState = stabilityState,
            stabilityRisk = stabilityRisk,
            rorTrend = rorTrend,
            heatPressure = heatPressure,
            airflowPressure = airflowPressure,
            capability = capability
        )

        return RoastAiBrainState(
            machineName = profile.name,
            phase = phase,
            phaseReason = phaseReason,
            energyState = energyState,
            energyTrend = energyTrend,
            stabilityState = stabilityState,
            stabilityRisk = stabilityRisk,
            rorTrend = rorTrend,
            heatPressure = heatPressure,
            airflowPressure = airflowPressure,
            controlLevel = capability.controlLevel,
            canActDirectly = canActDirectly,
            operatorSummary = operatorSummary,
            aiSummary = aiSummary
        )
    }

    fun toContext(
        machineProfile: MachineProfile,
        machineState: MachineState,
        telemetryFrame: MachineTelemetryFrame?,
        controlCapability: MachineControlCapability,
        energySnapshot: EnergySnapshot?,
        stabilityResult: RoastStabilityResult?,
        drivingAdvice: RoastDrivingAdvice?,
        decisionResult: DecisionEngine.DecisionResult?,
        controlPlan: MachineControlPlan?,
        executionSummary: MachineControlExecutionSummary? = null,
        styleGoal: RoastAiStyleGoal? = null,
        userPrompt: String = "",
        operatorNote: String = "",
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContexts.buildRealtimeCoachingContext(
            machineProfile = machineProfile,
            machineState = machineState,
            telemetryFrame = telemetryFrame,
            controlCapability = controlCapability,
            energySnapshot = energySnapshot,
            stabilityResult = stabilityResult,
            drivingAdvice = drivingAdvice,
            decisionResult = decisionResult,
            controlPlan = controlPlan,
            executionSummary = executionSummary,
            styleGoal = styleGoal,
            userPrompt = userPrompt,
            operatorNote = operatorNote,
            attachments = attachments
        )
    }

    private fun inferPhase(
        machineState: MachineState
    ): RoastAiPhase {
        val bt = machineState.beanTemp
        val elapsed = machineState.elapsedSec

        return when {
            elapsed <= 0 -> RoastAiPhase.PREHEAT
            elapsed < 25 -> RoastAiPhase.CHARGE
            bt < 60.0 -> RoastAiPhase.TURNING
            bt < 150.0 -> RoastAiPhase.DRYING
            bt < 185.0 -> RoastAiPhase.MAILLARD
            bt < 196.0 -> RoastAiPhase.FIRST_CRACK_WINDOW
            bt < 208.0 -> RoastAiPhase.DEVELOPMENT
            bt < 215.0 -> RoastAiPhase.DROP_WINDOW
            else -> RoastAiPhase.FINISHED
        }
    }

    private fun buildPhaseReason(
        machineState: MachineState,
        phase: RoastAiPhase
    ): String {
        return when (phase) {
            RoastAiPhase.PREHEAT ->
                "Elapsed time is zero or machine is not yet in roast motion."

            RoastAiPhase.CHARGE ->
                "Very early roast window immediately after charge."

            RoastAiPhase.TURNING ->
                "Bean temperature still sits in the early post-charge turning zone."

            RoastAiPhase.DRYING ->
                "Bean temperature indicates drying phase progression."

            RoastAiPhase.MAILLARD ->
                "Bean temperature has entered the Maillard browning range."

            RoastAiPhase.FIRST_CRACK_WINDOW ->
                "Bean temperature is approaching or entering first crack window."

            RoastAiPhase.DEVELOPMENT ->
                "Bean temperature suggests active development after crack approach."

            RoastAiPhase.DROP_WINDOW ->
                "Roast is in probable drop decision window."

            RoastAiPhase.FINISHED ->
                "Bean temperature is beyond normal roast finish window."

            RoastAiPhase.UNKNOWN ->
                "Phase could not be inferred from current state."
        }
    }

    private fun inferEnergyTrend(
        energy: EnergySnapshot?
    ): RoastAiTrend {
        val state = energy?.stateEnum ?: return RoastAiTrend.STABLE

        return when (state) {
            EnergyState.DEFICIT -> RoastAiTrend.FALLING
            EnergyState.LOW -> RoastAiTrend.SOFT
            EnergyState.MODERATE -> RoastAiTrend.STABLE
            EnergyState.BALANCED -> RoastAiTrend.RISING
            EnergyState.HIGH -> RoastAiTrend.AGGRESSIVE
        }
    }

    private fun inferStabilityRisk(
        stability: RoastStabilityResult?
    ): RoastAiRiskLevel {
        val score = stability?.score ?: return RoastAiRiskLevel.MEDIUM

        return when {
            score < 40 -> RoastAiRiskLevel.HIGH
            score < 70 -> RoastAiRiskLevel.MEDIUM
            else -> RoastAiRiskLevel.LOW
        }
    }

    private fun inferRorTrend(
        ror: Double
    ): RoastAiTrend {
        return when {
            ror < 2.0 -> RoastAiTrend.FALLING
            ror < 5.0 -> RoastAiTrend.SOFT
            ror < 8.5 -> RoastAiTrend.STABLE
            ror < 12.0 -> RoastAiTrend.RISING
            else -> RoastAiTrend.AGGRESSIVE
        }
    }

    private fun inferHeatPressure(
        profile: MachineProfile,
        machineState: MachineState
    ): String {
        val span = (profile.maxPowerW - profile.minPowerW).coerceAtLeast(1)
        val ratio = (machineState.powerW - profile.minPowerW).toDouble() / span.toDouble()

        return when {
            ratio >= 0.80 -> "Very High"
            ratio >= 0.60 -> "High"
            ratio >= 0.40 -> "Moderate"
            ratio >= 0.20 -> "Low"
            else -> "Very Low"
        }
    }

    private fun inferAirflowPressure(
        profile: MachineProfile,
        machineState: MachineState
    ): String {
        val span = (profile.maxAirflowPa - profile.minAirflowPa).coerceAtLeast(1)
        val ratio = (machineState.airflowPa - profile.minAirflowPa).toDouble() / span.toDouble()

        return when {
            ratio >= 0.80 -> "Very Strong"
            ratio >= 0.60 -> "Strong"
            ratio >= 0.40 -> "Moderate"
            ratio >= 0.20 -> "Light"
            else -> "Minimal"
        }
    }

    private fun buildOperatorSummary(
        phase: RoastAiPhase,
        machineState: MachineState,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        styleGoal: RoastAiStyleGoal?
    ): String {
        val styleText = styleGoal?.styleName ?: "No explicit style goal"
        val energyText = energy?.energyState ?: "Unknown energy"
        val stabilityText = stability?.stability ?: "Unknown stability"

        return """
Current roast is in $phase.
Bean temp is ${"%.1f".format(machineState.beanTemp)}℃ with ROR ${"%.1f".format(machineState.ror)}℃/min.
Energy is $energyText and stability is $stabilityText.
Style goal: $styleText.
        """.trimIndent()
    }

    private fun buildAiSummary(
        phase: RoastAiPhase,
        energyState: String,
        energyTrend: RoastAiTrend,
        stabilityState: String,
        stabilityRisk: RoastAiRiskLevel,
        rorTrend: RoastAiTrend,
        heatPressure: String,
        airflowPressure: String,
        capability: MachineControlCapability
    ): String {
        return """
AI sees the roast in $phase.
Energy state is $energyState with $energyTrend tendency.
Stability is $stabilityState with $stabilityRisk risk.
ROR trend is $rorTrend.
Heat pressure is $heatPressure and airflow pressure is $airflowPressure.
Control level is ${capability.controlLevel}.
        """.trimIndent()
    }
}
