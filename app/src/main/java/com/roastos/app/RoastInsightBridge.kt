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
            return "当前节奏整体稳定。"
        }

        val lower = normalized.lowercase(Locale.getDefault())

        return when {
            containsAny(
                lower,
                "stall",
                "slowing too much",
                "energy falling",
                "energy dropping",
                "deficit"
            ) -> "当前动能偏弱，推进正在放慢。"

            containsAny(
                lower,
                "flick",
                "overshoot",
                "too strong",
                "pushing too hard",
                "surging"
            ) -> "当前推进偏强，后段有上冲倾向。"

            containsAny(
                lower,
                "crash",
                "weakening into crack",
                "momentum collapsing"
            ) -> "中后段动能正在明显减弱。"

            containsAny(
                lower,
                "stable",
                "steady",
                "controlled",
                "balanced"
            ) -> "当前节奏整体稳定。"

            containsAny(
                lower,
                "energy building",
                "carrying forward",
                "good carry",
                "holding energy",
                "rising"
            ) -> "当前热量延续较平稳。"

            containsAny(
                lower,
                "maillard short",
                "short maillard",
                "development compressed"
            ) -> "中段发展略偏短。"

            containsAny(
                lower,
                "maillard long",
                "extended maillard",
                "development stretching"
            ) -> "中段发展略偏长。"

            containsAny(
                lower,
                "clean",
                "clarity",
                "clear cup"
            ) -> "当前节奏偏向更干净的杯测方向。"

            containsAny(
                lower,
                "sweet",
                "sweetness",
                "sweeter"
            ) -> "当前节奏有利于甜感保留。"

            containsAny(
                lower,
                "body",
                "heavier body",
                "weightier"
            ) -> "当前节奏偏向更厚的口感方向。"

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
            normalized.length <= 26 -> normalized
            else -> normalized.take(26).trimEnd() + "…"
        }

        if (base.isBlank()) {
            return "当前节奏整体稳定。"
        }

        return when {
            base.endsWith("。") || base.endsWith("！") || base.endsWith("…") -> base
            else -> "$base。"
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
