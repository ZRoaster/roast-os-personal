package com.roastos.app

object RoastInsightBridge {

    fun analyzeSnapshot(
        snapshot: RoastSessionBusSnapshot
    ): RoastInsightOutput {
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

        return RoastInsightEngine.analyze(
            profile = profile,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = null
        )
    }

    fun analyzeHistory(
        entry: RoastHistoryEntry
    ): RoastInsightOutput {
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

        return RoastInsightEngine.analyze(
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
            return "No clear observation."
        }

        val sentenceCut = normalized.indexOfFirst { it == '.' || it == '。' || it == '!' || it == '！' }
        val headline = when {
            sentenceCut > 0 -> normalized.substring(0, sentenceCut).trim()
            normalized.length <= 48 -> normalized
            else -> normalized.take(48).trimEnd() + "..."
        }

        return if (headline.isBlank()) "No clear observation." else headline
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
