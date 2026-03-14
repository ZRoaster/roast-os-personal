package com.roastos.app

object RoastInsightBridge {

    fun analyzeSnapshot(
        snapshot: RoastSessionBusSnapshot
    ): RoastInsightReport {
        val session = snapshot.session

        val machineState = MachineStateEngine.buildState(
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            beanTemp = session.lastBeanTemp,
            ror = session.lastRor,
            elapsedSec = session.lastElapsedSec,
            environmentTemp = 25.0,
            environmentHumidity = 50.0
        )

        return analyzeFromMachineState(machineState)
    }

    fun analyzeHistory(
        entry: RoastHistoryEntry
    ): RoastInsightReport {
        val machineState = MachineStateEngine.buildState(
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            beanTemp = 0.0,
            ror = entry.actualPreFcRor ?: 0.0,
            elapsedSec = entry.actualDropSec ?: entry.predictedDropSec ?: 0,
            environmentTemp = entry.envTemp,
            environmentHumidity = entry.envRh
        )

        return analyzeFromHistorySeed(
            machineState = machineState,
            seedTimeMillis = entry.createdAtMillis
        )
    }

    private fun analyzeFromMachineState(
        machineState: MachineState
    ): RoastInsightReport {
        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)

        RoastCurveEngineV3.reset()
        val now = System.currentTimeMillis()
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = now
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = now + 1000
        )
        RoastCurveEngineV3.record(
            bt = machineState.beanTemp,
            timeMillis = now + 2000
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

    private fun analyzeFromHistorySeed(
        machineState: MachineState,
        seedTimeMillis: Long
    ): RoastInsightReport {
        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)

        RoastCurveEngineV3.reset()
        RoastCurveEngineV3.record(
            bt = 100.0,
            timeMillis = seedTimeMillis
        )
        RoastCurveEngineV3.record(
            bt = 140.0,
            timeMillis = seedTimeMillis + 60_000
        )
        RoastCurveEngineV3.record(
            bt = 180.0,
            timeMillis = seedTimeMillis + 120_000
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
}
