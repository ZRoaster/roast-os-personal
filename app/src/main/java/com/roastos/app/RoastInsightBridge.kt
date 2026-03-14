package com.roastos.app

import java.util.Locale

object RoastInsightBridge {

    fun analyzeSnapshot(
        snapshot: RoastSessionBusSnapshot
    ) = run {
        val session = snapshot.session

        val plannerInput = AppState.lastPlannerInput
        val environmentTemp = plannerInput?.envTemp ?: 25.0
        val environmentHumidity = plannerInput?.envRH ?: 50.0

        val machineState = MachineStateEngine.buildState(
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            beanTemp = session.lastBeanTemp,
            ror = session.lastRor,
            elapsedSec = session.lastElapsedSec,
            environmentTemp = environmentTemp,
            environmentHumidity = environmentHumidity
        )

        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)

        RoastCurveEngineV3.reset()
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis()
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis() + 1000
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis() + 2000
        )

        val curvePrediction = RoastCurveEngineV3.predict()
        val stability = RoastStabilityEngine.evaluate(curvePrediction)

        RoastInsightEngine.analyze(
            profile = profile,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = null
        )
    }

    fun analyzeHistory(
        entry: RoastHistoryEntry
    ) = run {
        val beanTempEstimate = estimateBeanTempFromHistory(entry)
        val rorEstimate = entry.actualPreFcRor ?: 0.0
        val elapsedEstimate = entry.actualDropSec
            ?: entry.actualFcSec
            ?: entry.predictedDropSec
            ?: entry.predictedFcSec
            ?: 0

        val machineState = MachineStateEngine.buildState(
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            beanTemp = beanTempEstimate,
            ror = rorEstimate,
            elapsedSec = elapsedEstimate,
            environmentTemp = entry.envTemp,
            environmentHumidity = entry.envRh
        )

        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)

        RoastCurveEngineV3.reset()
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis()
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis() + 1000
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = System.currentTimeMillis() + 2000
        )

        val curvePrediction = RoastCurveEngineV3.predict()
        val stability = RoastStabilityEngine.evaluate(curvePrediction)

        RoastInsightEngine.analyze(
            profile = profile,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = null
        )
    }

    fun quietSummaryForSnapshot(
        snapshot: RoastSessionBusSnapshot
    ): String {
        return analyzeSnapshot(snapshot).quietSummary
    }

    fun quietSummaryForHistory(
        entry: RoastHistoryEntry
    ): String {
        return analyzeHistory(entry).quietSummary
    }

    fun observationHeadlineForSnapshot(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val insight = analyzeSnapshot(snapshot)
        return buildObservationHeadline(insight.quietSummary)
    }

    fun observationHeadlineForHistory(
        entry: RoastHistoryEntry
    ): String {
        val insight = analyzeHistory(entry)
        return buildObservationHeadline(insight.quietSummary)
    }

    private fun buildObservationHeadline(
        quietSummary: String
    ): String {
        val normalized = quietSummary
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) {
            return "Roast remains stable under current pace."
        }

        val lower = normalized.lowercase(Locale.getDefault())

        return when {
            containsAny(lower, "stall", "slowing too much", "energy falling", "energy dropping") ->
                "Momentum is weakening under current pace."

            containsAny(lower, "flick", "overshoot", "too strong", "pushing too hard", "surging") ->
                "Late-phase push looks stronger than baseline."

            containsAny(lower, "crash", "weakening into crack", "momentum collapsing") ->
                "Momentum may fade too quickly into late phase."

            containsAny(lower, "stable", "steady", "controlled", "balanced") ->
                "Roast remains stable under current pace."

            containsAny(lower, "energy building", "carrying forward", "good carry", "holding energy") ->
                "Energy is carrying forward steadily."

            containsAny(lower, "maillard short", "short maillard", "development compressed") ->
                "Mid-phase development looks slightly compressed."

            containsAny(lower, "maillard long", "extended maillard", "development stretching") ->
                "Mid-phase development looks slightly extended."

            containsAny(lower, "clean", "clarity", "clear cup") ->
                "Current pace supports a cleaner cup direction."

            containsAny(lower, "sweet", "sweetness", "sweeter") ->
                "Current pace supports sweetness retention."

            containsAny(lower, "body", "heavier body", "weightier") ->
                "Current pace supports a heavier body direction."

            else -> fallbackHeadline(normalized)
        }
    }

    private fun fallbackHeadline(
        normalized: String
    ): String {
        val sentenceCut = normalized.indexOfFirst {
            it == '.' || it == '。' || it == '!' || it == '！'
        }

        val base = when {
            sentenceCut > 0 -> normalized.substring(0, sentenceCut).trim()
            normalized.length <= 52 -> normalized
            else -> normalized.take(52).trimEnd() + "..."
        }

        return if (base.isBlank()) {
            "Roast remains stable under current pace."
        } else {
            base
        }
    }

    private fun containsAny(
        text: String,
        vararg patterns: String
    ): Boolean {
        return patterns.any { pattern -> pattern in text }
    }

    private fun estimateBeanTempFromHistory(
        entry: RoastHistoryEntry
    ): Double {
        val fc = entry.actualFcSec ?: entry.predictedFcSec
        val drop = entry.actualDropSec ?: entry.predictedDropSec

        return when {
            drop != null -> 205.0
            fc != null -> 196.0
            else -> 175.0
        }
    }
}
