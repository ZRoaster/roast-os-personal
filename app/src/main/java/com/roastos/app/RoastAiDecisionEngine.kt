package com.roastos.app

enum class RoastAiRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class RoastAiDecision(
    val summary: String,
    val rationale: String,
    val riskLevel: RoastAiRiskLevel,
    val recommendedHeatDeltaW: Int? = null,
    val recommendedAirflowDeltaPa: Int? = null,
    val recommendedDrumDeltaRpm: Int? = null,
    val shouldEscalateToHuman: Boolean = false,
    val suggestedCommand: MachineControlCommand? = null
) {
    fun detail(): String {
        return """
AI Decision

Summary
$summary

Rationale
$rationale

Risk Level
$riskLevel

Heat Delta
${recommendedHeatDeltaW ?: "-"}

Airflow Delta
${recommendedAirflowDeltaPa ?: "-"}

Drum Delta
${recommendedDrumDeltaRpm ?: "-"}

Escalate To Human
${if (shouldEscalateToHuman) "Yes" else "No"}

Suggested Command
${suggestedCommand?.summary() ?: "-"}
        """.trimIndent()
    }
}

object RoastAiDecisionEngine {

    fun decide(
        context: RoastAiContext,
        aiResponse: RoastAiResponse?,
        profile: MachineProfile,
        capability: MachineControlCapability,
        machineState: MachineState,
        energy: EnergySnapshot? = null,
        stability: RoastStabilityResult? = null
    ): RoastAiDecision {

        val responseText = buildResponseText(aiResponse)

        val heatDelta = inferHeatDelta(responseText, energy, stability)
        val airflowDelta = inferAirflowDelta(responseText, energy, stability)
        val drumDelta = inferDrumDelta(responseText)

        val risk = inferRiskLevel(
            machineState = machineState,
            energy = energy,
            stability = stability,
            responseText = responseText
        )

        val rationale = buildRationale(
            context = context,
            aiResponse = aiResponse,
            energy = energy,
            stability = stability,
            heatDelta = heatDelta,
            airflowDelta = airflowDelta,
            drumDelta = drumDelta
        )

        val suggestedCommand = buildSuggestedCommand(
            profile = profile,
            capability = capability,
            machineState = machineState,
            heatDelta = heatDelta,
            airflowDelta = airflowDelta,
            drumDelta = drumDelta,
            rationale = rationale
        )

        val summary = buildSummary(
            aiResponse = aiResponse,
            heatDelta = heatDelta,
            airflowDelta = airflowDelta,
            drumDelta = drumDelta,
            risk = risk
        )

        return RoastAiDecision(
            summary = summary,
            rationale = rationale,
            riskLevel = risk,
            recommendedHeatDeltaW = heatDelta,
            recommendedAirflowDeltaPa = airflowDelta,
            recommendedDrumDeltaRpm = drumDelta,
            shouldEscalateToHuman = shouldEscalate(risk, capability),
            suggestedCommand = suggestedCommand
        )
    }

    private fun buildResponseText(aiResponse: RoastAiResponse?): String {
        return buildString {
            append(aiResponse?.summary() ?: "")
            if (isBlank()) append("")
        }
    }

    private fun inferHeatDelta(
        responseText: String,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?
    ): Int? {

        if (containsAny(responseText, "reduce heat", "lower heat", "cut heat")) {
            return -60
        }

        if (containsAny(responseText, "increase heat", "add heat", "more heat")) {
            return 60
        }

        if (energy?.stateEnum == EnergyState.DEFICIT) {
            return 50
        }

        if (energy?.stateEnum == EnergyState.HIGH) {
            return -40
        }

        if (stability?.stability?.contains("Unstable", ignoreCase = true) == true) {
            return -20
        }

        return null
    }

    private fun inferAirflowDelta(
        responseText: String,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?
    ): Int? {

        if (containsAny(responseText, "increase airflow", "more airflow", "open airflow")) {
            return 2
        }

        if (containsAny(responseText, "reduce airflow", "less airflow", "close airflow")) {
            return -2
        }

        if (energy?.stateEnum == EnergyState.HIGH &&
            stability?.stability?.contains("Unstable", ignoreCase = true) == true
        ) {
            return 2
        }

        return null
    }

    private fun inferDrumDelta(
        responseText: String
    ): Int? {

        if (containsAny(responseText, "increase drum", "faster drum")) {
            return 3
        }

        if (containsAny(responseText, "reduce drum", "slower drum")) {
            return -3
        }

        return null
    }

    private fun inferRiskLevel(
        machineState: MachineState,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        responseText: String
    ): RoastAiRiskLevel {

        if (containsAny(responseText, "emergency", "stop roast", "critical")) {
            return RoastAiRiskLevel.HIGH
        }

        if (machineState.ror < 2.0 || machineState.ror > 14.0) {
            return RoastAiRiskLevel.HIGH
        }

        if (energy?.stateEnum == EnergyState.DEFICIT || energy?.stateEnum == EnergyState.HIGH) {
            return RoastAiRiskLevel.MEDIUM
        }

        if (stability?.stability?.contains("Unstable", ignoreCase = true) == true) {
            return RoastAiRiskLevel.MEDIUM
        }

        return RoastAiRiskLevel.LOW
    }

    private fun buildRationale(
        context: RoastAiContext,
        aiResponse: RoastAiResponse?,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        heatDelta: Int?,
        airflowDelta: Int?,
        drumDelta: Int?
    ): String {

        val aiText = aiResponse?.compact() ?: "No AI response."
        val energyText = energy?.summary ?: "No energy snapshot."
        val stabilityText = stability?.summary ?: "No stability result."

        return """
AI rationale synthesized from current roast context.

Intent
${context.intent}

AI Response
$aiText

Energy
$energyText

Stability
$stabilityText

Interpreted Adjustments
Heat=${heatDelta ?: "-"}, Airflow=${airflowDelta ?: "-"}, Drum=${drumDelta ?: "-"}
        """.trimIndent()
    }

    private fun buildSuggestedCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        machineState: MachineState,
        heatDelta: Int?,
        airflowDelta: Int?,
        drumDelta: Int?,
        rationale: String
    ): MachineControlCommand? {

        if (heatDelta != null) {
            return MachineControlCommands.buildHeatCommand(
                profile = profile,
                capability = capability,
                targetHeatW = (machineState.powerW + heatDelta)
                    .coerceIn(profile.minPowerW, profile.maxPowerW),
                reason = rationale
            )
        }

        if (airflowDelta != null) {
            return MachineControlCommands.buildAirflowCommand(
                profile = profile,
                capability = capability,
                targetAirflowPa = (machineState.airflowPa + airflowDelta)
                    .coerceIn(profile.minAirflowPa, profile.maxAirflowPa),
                reason = rationale
            )
        }

        if (drumDelta != null) {
            return MachineControlCommands.buildDrumCommand(
                profile = profile,
                capability = capability,
                targetDrumRpm = (machineState.drumRpm + drumDelta)
                    .coerceIn(profile.minDrumRpm, profile.maxDrumRpm),
                reason = rationale
            )
        }

        return MachineControlCommands.buildHoldCommand(
            profile = profile,
            capability = capability,
            reason = "AI suggests maintaining current settings."
        )
    }

    private fun buildSummary(
        aiResponse: RoastAiResponse?,
        heatDelta: Int?,
        airflowDelta: Int?,
        drumDelta: Int?,
        risk: RoastAiRiskLevel
    ): String {

        val primary = when {
            heatDelta != null -> "Heat ${formatDelta(heatDelta)}W"
            airflowDelta != null -> "Airflow ${formatDelta(airflowDelta)}Pa"
            drumDelta != null -> "Drum ${formatDelta(drumDelta)}rpm"
            else -> "Hold current settings"
        }

        return """
AI Decision Summary

Primary Advice
$primary

Risk
$risk

AI Basis
${aiResponse?.compact() ?: "No AI response"}
        """.trimIndent()
    }

    private fun shouldEscalate(
        risk: RoastAiRiskLevel,
        capability: MachineControlCapability
    ): Boolean {
        return risk == RoastAiRiskLevel.HIGH || capability.requiresConfirmation
    }

    private fun containsAny(text: String, vararg patterns: String): Boolean {
        val lower = text.lowercase()
        return patterns.any { lower.contains(it.lowercase()) }
    }

    private fun formatDelta(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
