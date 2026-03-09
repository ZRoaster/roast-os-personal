package com.roastos.app

data class MachineControlPlan(
    val machineName: String,
    val primaryCommand: MachineControlCommand,
    val secondaryCommand: MachineControlCommand? = null,
    val advisoryText: String,
    val confidence: Int,
    val summary: String
)

object MachineControlPlanner {

    fun buildPlan(
        profile: MachineProfile,
        capability: MachineControlCapability,
        machineState: MachineState,
        energy: EnergySnapshot,
        decision: DecisionEngine.DecisionResult
    ): MachineControlPlan {

        val primary = when {
            decision.suggestion.contains("emergency", ignoreCase = true) &&
                capability.supportsEmergencyStop -> {
                MachineControlCommands.buildEmergencyStopCommand(
                    profile = profile,
                    capability = capability,
                    reason = decision.reason
                )
            }

            decision.suggestion.contains("increase heat", ignoreCase = true) -> {
                val targetHeat = (machineState.powerW + heatStepW(profile, decision.severity))
                    .coerceIn(profile.minPowerW, profile.maxPowerW)

                MachineControlCommands.buildHeatCommand(
                    profile = profile,
                    capability = capability,
                    targetHeatW = targetHeat,
                    reason = decision.reason
                )
            }

            decision.suggestion.contains("reduce heat", ignoreCase = true) -> {
                val targetHeat = (machineState.powerW - heatStepW(profile, decision.severity))
                    .coerceIn(profile.minPowerW, profile.maxPowerW)

                MachineControlCommands.buildHeatCommand(
                    profile = profile,
                    capability = capability,
                    targetHeatW = targetHeat,
                    reason = decision.reason
                )
            }

            decision.suggestion.contains("increase airflow", ignoreCase = true) -> {
                val targetAir = (machineState.airflowPa + airflowStepPa(profile, decision.severity))
                    .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

                MachineControlCommands.buildAirflowCommand(
                    profile = profile,
                    capability = capability,
                    targetAirflowPa = targetAir,
                    reason = decision.reason
                )
            }

            decision.suggestion.contains("reduce airflow", ignoreCase = true) -> {
                val targetAir = (machineState.airflowPa - airflowStepPa(profile, decision.severity))
                    .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

                MachineControlCommands.buildAirflowCommand(
                    profile = profile,
                    capability = capability,
                    targetAirflowPa = targetAir,
                    reason = decision.reason
                )
            }

            else -> {
                MachineControlCommands.buildHoldCommand(
                    profile = profile,
                    capability = capability,
                    reason = decision.reason
                )
            }
        }

        val secondary = buildSecondaryCommand(
            profile = profile,
            capability = capability,
            machineState = machineState,
            energy = energy,
            decision = decision,
            primary = primary
        )

        val confidence = buildConfidence(
            capability = capability,
            energy = energy,
            decision = decision,
            primary = primary
        )

        val advisoryText = buildAdvisoryText(
            capability = capability,
            decision = decision,
            primary = primary,
            secondary = secondary
        )

        val summary = buildSummary(
            profile = profile,
            capability = capability,
            decision = decision,
            energy = energy,
            primary = primary,
            secondary = secondary,
            confidence = confidence,
            advisoryText = advisoryText
        )

        return MachineControlPlan(
            machineName = profile.name,
            primaryCommand = primary,
            secondaryCommand = secondary,
            advisoryText = advisoryText,
            confidence = confidence,
            summary = summary
        )
    }

    private fun buildSecondaryCommand(
        profile: MachineProfile,
        capability: MachineControlCapability,
        machineState: MachineState,
        energy: EnergySnapshot,
        decision: DecisionEngine.DecisionResult,
        primary: MachineControlCommand
    ): MachineControlCommand? {

        if (primary.type == MachineCommandType.EMERGENCY_STOP) {
            return null
        }

        if (decision.suggestion.contains("increase heat", ignoreCase = true) &&
            energy.airflowLevel == "Very Strong" &&
            capability.canSetAirflow
        ) {
            val targetAir = (machineState.airflowPa - airflowStepPa(profile, "MEDIUM"))
                .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

            return MachineControlCommands.buildAirflowCommand(
                profile = profile,
                capability = capability,
                targetAirflowPa = targetAir,
                reason = "Reduce airflow to preserve heat while recovering momentum"
            )
        }

        if (decision.suggestion.contains("reduce heat", ignoreCase = true) &&
            energy.rorTrend.contains("Aggressive", ignoreCase = true) &&
            capability.canSetAirflow
        ) {
            val targetAir = (machineState.airflowPa + airflowStepPa(profile, "MEDIUM"))
                .coerceIn(profile.minAirflowPa, profile.maxAirflowPa)

            return MachineControlCommands.buildAirflowCommand(
                profile = profile,
                capability = capability,
                targetAirflowPa = targetAir,
                reason = "Increase airflow to help soften aggressive ROR"
            )
        }

        return null
    }

    private fun buildConfidence(
        capability: MachineControlCapability,
        energy: EnergySnapshot,
        decision: DecisionEngine.DecisionResult,
        primary: MachineControlCommand
    ): Int {

        var score = 70

        if (primary.status == MachineCommandStatus.BLOCKED) {
            score -= 30
        }

        if (capability.requiresConfirmation) {
            score -= 10
        }

        if (decision.severity.equals("HIGH", ignoreCase = true)) {
            score += 8
        }

        when (energy.stateEnum) {
            EnergyState.DEFICIT -> score += 6
            EnergyState.HIGH -> score += 4
            EnergyState.BALANCED -> score += 2
            EnergyState.MODERATE -> score += 0
            EnergyState.LOW -> score += 3
        }

        return score.coerceIn(1, 99)
    }

    private fun buildAdvisoryText(
        capability: MachineControlCapability,
        decision: DecisionEngine.DecisionResult,
        primary: MachineControlCommand,
        secondary: MachineControlCommand?
    ): String {

        val modeText = when (capability.controlLevel) {
            MachineControlLevel.READ_ONLY -> "Read-only machine"
            MachineControlLevel.ASSISTED -> "Assisted-control machine"
            MachineControlLevel.FULL_CONTROL -> "Full-control machine"
        }

        val primaryText = primary.summary()

        val secondaryText = secondary?.summary() ?: "No secondary command"

        return """
Control Planner Advisory

Mode
$modeText

Decision
${decision.suggestion}

Primary
$primaryText

Secondary
$secondaryText
        """.trimIndent()
    }

    private fun buildSummary(
        profile: MachineProfile,
        capability: MachineControlCapability,
        decision: DecisionEngine.DecisionResult,
        energy: EnergySnapshot,
        primary: MachineControlCommand,
        secondary: MachineControlCommand?,
        confidence: Int,
        advisoryText: String
    ): String {

        return """
Machine Control Plan

Machine
${profile.name}

Control Level
${capability.controlLevel}

Decision
${decision.suggestion}

Decision Severity
${decision.severity}

Energy State
${energy.energyState}

Primary Command
${primary.type} / ${primary.status}

Secondary Command
${secondary?.type ?: "-"}

Confidence
$confidence

Advisory
$advisoryText
        """.trimIndent()
    }

    private fun heatStepW(
        profile: MachineProfile,
        severity: String
    ): Int {
        val span = profile.maxPowerW - profile.minPowerW
        return when (severity.uppercase()) {
            "HIGH" -> (span * 0.12).toInt().coerceAtLeast(40)
            "MEDIUM" -> (span * 0.08).toInt().coerceAtLeast(25)
            else -> (span * 0.05).toInt().coerceAtLeast(15)
        }
    }

    private fun airflowStepPa(
        profile: MachineProfile,
        severity: String
    ): Int {
        val span = profile.maxAirflowPa - profile.minAirflowPa
        return when (severity.uppercase()) {
            "HIGH" -> (span * 0.15).toInt().coerceAtLeast(2)
            "MEDIUM" -> (span * 0.10).toInt().coerceAtLeast(1)
            else -> (span * 0.06).toInt().coerceAtLeast(1)
        }
    }
}
