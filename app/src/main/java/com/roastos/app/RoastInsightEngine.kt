package com.roastos.app

enum class RoastInsightSeverity {
    QUIET,
    NOTICE,
    WATCH,
    ALERT
}

enum class RoastInsightType {
    OBSERVATION,
    CAUSE,
    CONSEQUENCE,
    POSSIBILITY
}

enum class RoastFlavorDirection {
    CLARITY,
    SWEETNESS,
    BODY,
    BALANCE,
    EXPERIMENTAL
}

data class RoastInsight(
    val type: RoastInsightType,
    val severity: RoastInsightSeverity,
    val title: String,
    val message: String
) {
    fun summary(): String {
        return """
$type
$title

$message

Severity
$severity
        """.trimIndent()
    }
}

data class RoastPossibility(
    val direction: RoastFlavorDirection,
    val title: String,
    val description: String,
    val rationale: String
) {
    fun summary(): String {
        return """
$title

Direction
$direction

Description
$description

Rationale
$rationale
        """.trimIndent()
    }
}

data class RoastInsightReport(
    val phaseLabel: String,
    val energyLabel: String,
    val stabilityLabel: String,
    val momentumLabel: String,
    val quietSummary: String,
    val observations: List<RoastInsight>,
    val possibilities: List<RoastPossibility>
) {
    fun summary(): String {
        val observationText = if (observations.isEmpty()) {
            "-"
        } else {
            observations.joinToString("\n\n") { it.summary() }
        }

        val possibilityText = if (possibilities.isEmpty()) {
            "-"
        } else {
            possibilities.joinToString("\n\n") { it.summary() }
        }

        return """
Roast Insight Report

Phase
$phaseLabel

Energy
$energyLabel

Stability
$stabilityLabel

Momentum
$momentumLabel

Quiet Summary
$quietSummary

Observations
$observationText

Possibilities
$possibilityText
        """.trimIndent()
    }
}

object RoastInsightEngine {

    fun analyze(
        profile: MachineProfile,
        machineState: MachineState,
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        styleGoal: RoastAiStyleGoal? = null
    ): RoastInsightReport {

        val phase = inferPhaseLabel(machineState)
        val energyLabel = energy?.energyState ?: "Unknown energy"
        val stabilityLabel = stability?.stability ?: "Unknown stability"
        val momentumLabel = inferMomentumLabel(machineState.ror)

        val observations = mutableListOf<RoastInsight>()

        observations += buildPhaseObservation(
            phaseLabel = phase,
            machineState = machineState
        )

        buildEnergyObservation(
            phaseLabel = phase,
            energy = energy
        )?.let { observations += it }

        buildStabilityObservation(
            stability = stability
        )?.let { observations += it }

        buildRorObservation(
            phaseLabel = phase,
            machineState = machineState
        )?.let { observations += it }

        buildHeatPressureObservation(
            profile = profile,
            machineState = machineState
        )?.let { observations += it }

        buildAirflowObservation(
            profile = profile,
            machineState = machineState
        )?.let { observations += it }

        buildCauseObservation(
            energy = energy,
            stability = stability,
            machineState = machineState
        )?.let { observations += it }

        buildConsequenceObservation(
            energy = energy,
            stability = stability,
            machineState = machineState,
            styleGoal = styleGoal
        )?.let { observations += it }

        val possibilities = buildPossibilities(
            energy = energy,
            machineState = machineState,
            styleGoal = styleGoal
        )

        return RoastInsightReport(
            phaseLabel = phase,
            energyLabel = energyLabel,
            stabilityLabel = stabilityLabel,
            momentumLabel = momentumLabel,
            quietSummary = buildQuietSummary(
                phaseLabel = phase,
                energyLabel = energyLabel,
                stabilityLabel = stabilityLabel,
                momentumLabel = momentumLabel
            ),
            observations = observations,
            possibilities = possibilities
        )
    }

    private fun inferPhaseLabel(
        machineState: MachineState
    ): String {
        val bt = machineState.beanTemp
        val elapsed = machineState.elapsedSec

        return when {
            elapsed <= 0 -> "Preheat"
            elapsed < 25 -> "Charge"
            bt < 60.0 -> "Turning"
            bt < 150.0 -> "Drying"
            bt < 185.0 -> "Maillard"
            bt < 196.0 -> "First Crack Window"
            bt < 208.0 -> "Development"
            bt < 215.0 -> "Drop Window"
            else -> "Finished"
        }
    }

    private fun inferMomentumLabel(
        ror: Double
    ): String {
        return when {
            ror < 2.0 -> "Falling"
            ror < 5.0 -> "Soft"
            ror < 8.5 -> "Stable"
            ror < 12.0 -> "Rising"
            else -> "Aggressive"
        }
    }

    private fun buildQuietSummary(
        phaseLabel: String,
        energyLabel: String,
        stabilityLabel: String,
        momentumLabel: String
    ): String {
        return "$phaseLabel · $energyLabel · $stabilityLabel · $momentumLabel"
    }

    private fun buildPhaseObservation(
        phaseLabel: String,
        machineState: MachineState
    ): RoastInsight {
        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = RoastInsightSeverity.QUIET,
            title = "Current phase",
            message = "Roast is in $phaseLabel at ${"%.1f".format(machineState.beanTemp)}℃ with RoR ${"%.1f".format(machineState.ror)}℃/min."
        )
    }

    private fun buildEnergyObservation(
        phaseLabel: String,
        energy: EnergySnapshot?
    ): RoastInsight? {
        val energyState = energy?.energyState ?: return null
        val stateEnum = energy.stateEnum

        val severity = when (stateEnum) {
            EnergyState.DEFICIT -> RoastInsightSeverity.ALERT
            EnergyState.LOW -> RoastInsightSeverity.WATCH
            EnergyState.MODERATE -> RoastInsightSeverity.QUIET
            EnergyState.BALANCED -> RoastInsightSeverity.QUIET
            EnergyState.HIGH -> RoastInsightSeverity.WATCH
        }

        val message = when (stateEnum) {
            EnergyState.DEFICIT ->
                "Energy is low in $phaseLabel. Roast may lose momentum if not protected."

            EnergyState.LOW ->
                "Energy is slightly soft in $phaseLabel. This can support delicacy but may reduce development pressure."

            EnergyState.MODERATE ->
                "Energy sits in a moderate zone. Roast is moving with a controlled pace."

            EnergyState.BALANCED ->
                "Energy looks balanced. This usually supports an even and readable roast rhythm."

            EnergyState.HIGH ->
                "Energy is elevated in $phaseLabel. Useful for drive, but watch for overshoot and compressed development."
        }

        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = severity,
            title = "Energy state",
            message = message
        )
    }

    private fun buildStabilityObservation(
        stability: RoastStabilityResult?
    ): RoastInsight? {
        stability ?: return null

        val severity = when {
            stability.score < 40 -> RoastInsightSeverity.ALERT
            stability.score < 70 -> RoastInsightSeverity.WATCH
            else -> RoastInsightSeverity.QUIET
        }

        val message = when {
            stability.score < 40 ->
                "Roast stability is fragile. Trend changes may propagate quickly."

            stability.score < 70 ->
                "Roast stability is workable but not fully settled. Small changes deserve attention."

            else ->
                "Roast stability is healthy. The system looks readable and cooperative."
        }

        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = severity,
            title = "Stability",
            message = message
        )
    }

    private fun buildRorObservation(
        phaseLabel: String,
        machineState: MachineState
    ): RoastInsight? {
        val ror = machineState.ror

        val severity = when {
            ror < 2.0 -> RoastInsightSeverity.ALERT
            ror < 5.0 -> RoastInsightSeverity.WATCH
            ror < 12.0 -> RoastInsightSeverity.QUIET
            else -> RoastInsightSeverity.WATCH
        }

        val message = when {
            ror < 2.0 ->
                "RoR is very soft in $phaseLabel. Momentum is close to flattening."

            ror < 5.0 ->
                "RoR is gentle in $phaseLabel. This can increase sweetness but may soften structure."

            ror < 12.0 ->
                "RoR is in a healthy working zone. Roast rhythm remains readable."

            else ->
                "RoR is strong in $phaseLabel. This can build intensity quickly, but watch for flavor compression."
        }

        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = severity,
            title = "RoR momentum",
            message = message
        )
    }

    private fun buildHeatPressureObservation(
        profile: MachineProfile,
        machineState: MachineState
    ): RoastInsight? {
        val span = (profile.maxPowerW - profile.minPowerW).coerceAtLeast(1)
        val ratio = (machineState.powerW - profile.minPowerW).toDouble() / span.toDouble()

        val label = when {
            ratio >= 0.80 -> "very high"
            ratio >= 0.60 -> "high"
            ratio >= 0.40 -> "moderate"
            ratio >= 0.20 -> "low"
            else -> "very low"
        }

        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = RoastInsightSeverity.QUIET,
            title = "Heat pressure",
            message = "Applied heat sits in a $label range at ${machineState.powerW}W."
        )
    }

    private fun buildAirflowObservation(
        profile: MachineProfile,
        machineState: MachineState
    ): RoastInsight? {
        val span = (profile.maxAirflowPa - profile.minAirflowPa).coerceAtLeast(1)
        val ratio = (machineState.airflowPa - profile.minAirflowPa).toDouble() / span.toDouble()

        val label = when {
            ratio >= 0.80 -> "very open"
            ratio >= 0.60 -> "open"
            ratio >= 0.40 -> "moderate"
            ratio >= 0.20 -> "gentle"
            else -> "minimal"
        }

        return RoastInsight(
            type = RoastInsightType.OBSERVATION,
            severity = RoastInsightSeverity.QUIET,
            title = "Airflow pressure",
            message = "Airflow is running in a $label range at ${machineState.airflowPa}Pa."
        )
    }

    private fun buildCauseObservation(
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        machineState: MachineState
    ): RoastInsight? {
        val message = when {
            energy?.stateEnum == EnergyState.HIGH && machineState.ror > 10.0 ->
                "Strong energy and elevated RoR suggest the system is carrying more momentum than usual."

            energy?.stateEnum == EnergyState.DEFICIT && machineState.ror < 4.0 ->
                "Soft energy and weak RoR suggest heat transfer is no longer driving the roast confidently."

            stability != null && stability.score < 50 ->
                "Low stability score suggests the roast is reacting unevenly to current conditions."

            else -> return null
        }

        val severity = when {
            energy?.stateEnum == EnergyState.DEFICIT -> RoastInsightSeverity.ALERT
            energy?.stateEnum == EnergyState.HIGH -> RoastInsightSeverity.WATCH
            else -> RoastInsightSeverity.WATCH
        }

        return RoastInsight(
            type = RoastInsightType.CAUSE,
            severity = severity,
            title = "Likely cause",
            message = message
        )
    }

    private fun buildConsequenceObservation(
        energy: EnergySnapshot?,
        stability: RoastStabilityResult?,
        machineState: MachineState,
        styleGoal: RoastAiStyleGoal?
    ): RoastInsight? {
        val style = styleGoal?.styleName ?: "current target"

        val message = when {
            energy?.stateEnum == EnergyState.HIGH && machineState.ror > 10.0 ->
                "If this continues, roast character may outrun nuance and make $style harder to express cleanly."

            energy?.stateEnum == EnergyState.DEFICIT && machineState.ror < 4.0 ->
                "If this continues, structure may flatten and sweetness may develop without enough lift."

            stability != null && stability.score < 50 ->
                "If this continues, flavor definition may become less precise and harder to repeat."

            else -> return null
        }

        return RoastInsight(
            type = RoastInsightType.CONSEQUENCE,
            severity = RoastInsightSeverity.WATCH,
            title = "Possible result",
            message = message
        )
    }

    private fun buildPossibilities(
        energy: EnergySnapshot?,
        machineState: MachineState,
        styleGoal: RoastAiStyleGoal?
    ): List<RoastPossibility> {
        val list = mutableListOf<RoastPossibility>()

        list += RoastPossibility(
            direction = RoastFlavorDirection.CLARITY,
            title = "Clarity focus",
            description = "Keep the roast more open and transparent.",
            rationale = "Works best when momentum remains readable and airflow supports a cleaner structure."
        )

        list += RoastPossibility(
            direction = RoastFlavorDirection.SWEETNESS,
            title = "Sweet balance",
            description = "Protect sweetness with a smooth middle phase and controlled finish.",
            rationale = "Favors stable Maillard energy and avoids sharp momentum breaks."
        )

        list += RoastPossibility(
            direction = RoastFlavorDirection.BODY,
            title = "Body emphasis",
            description = "Build a denser, rounder cup with stronger roast presence.",
            rationale = "Requires enough energy continuity to support deeper structure without flattening the finish."
        )

        if (styleGoal != null) {
            list += RoastPossibility(
                direction = RoastFlavorDirection.EXPERIMENTAL,
                title = "Current style path",
                description = "Lean into ${styleGoal.styleName}.",
                rationale = "Requested direction: ${styleGoal.flavorDirection}."
            )
        }

        if (energy?.stateEnum == EnergyState.HIGH && machineState.ror > 10.0) {
            return list.map {
                if (it.direction == RoastFlavorDirection.BODY) {
                    it.copy(
                        rationale = it.rationale + " Current energy already supports stronger development pressure."
                    )
                } else {
                    it
                }
            }
        }

        if (energy?.stateEnum == EnergyState.DEFICIT && machineState.ror < 4.0) {
            return list.map {
                if (it.direction == RoastFlavorDirection.CLARITY) {
                    it.copy(
                        rationale = it.rationale + " Soft energy may protect delicacy, but watch for flattening."
                    )
                } else {
                    it
                }
            }
        }

        return list
    }

    fun primaryInsight(
        report: RoastInsightReport
    ): RoastInsight? {
        return report.observations
            .sortedByDescending { severityWeight(it.severity) }
            .firstOrNull()
    }

    fun quietScreenText(
        report: RoastInsightReport
    ): String {
        val primary = primaryInsight(report)

        return if (primary == null) {
            report.quietSummary
        } else {
            """
${primary.title}

${primary.message}
            """.trimIndent()
        }
    }

    private fun severityWeight(
        severity: RoastInsightSeverity
    ): Int {
        return when (severity) {
            RoastInsightSeverity.QUIET -> 0
            RoastInsightSeverity.NOTICE -> 1
            RoastInsightSeverity.WATCH -> 2
            RoastInsightSeverity.ALERT -> 3
        }
    }
}
