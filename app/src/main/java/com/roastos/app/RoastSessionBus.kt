package com.roastos.app

data class RoastSessionBusSnapshot(
    val session: RoastSessionState,
    val phaseState: RoastPhaseDetectionState,
    val phaseSummary: String,
    val companion: RoastCompanionMessage,
    val validation: RoastValidationResult,
    val log: RoastLog,
    val logText: String,
    val historySummary: String,
    val recentRoasts: List<RoastHistoryEntry>
)

object RoastSessionBus {

    private var lastSnapshot: RoastSessionBusSnapshot? = null

    fun current(): RoastSessionBusSnapshot? {
        return lastSnapshot
    }

    fun reset() {
        lastSnapshot = null
        RoastSessionEngine.reset()
        RoastPhaseDetectionEngine.reset()
        RoastLogEngine.reset()
    }

    fun tick(): RoastSessionBusSnapshot {

        val session = RoastSessionEngine.currentState()

        val phaseState = RoastPhaseDetectionEngine.update(session)
        val phaseSummary = RoastPhaseDetectionEngine.summary()

        RoastLogEngine.update(session)

        val log = RoastLogEngine.buildLog(session)
        val logText = RoastLogEngine.buildLogText(session)

        val tempCompanion = RoastCompanionMessage(
            title = "Pending",
            body = "",
            phaseLabel = RoastSessionEngine.phaseLabel(session.phase),
            riskLevel = "none"
        )

        val validation = RoastSessionValidator.validate(
            RoastSessionBusSnapshot(
                session = session,
                phaseState = phaseState,
                phaseSummary = phaseSummary,
                companion = tempCompanion,
                validation = RoastValidationResult(emptyList()),
                log = log,
                logText = logText,
                historySummary = RoastHistoryEngine.summary(),
                recentRoasts = RoastHistoryEngine.all().take(3)
            )
        )

        val companion = RoastCompanionEngine.buildMessage(session)

        val snapshot = RoastSessionBusSnapshot(
            session = session,
            phaseState = phaseState,
            phaseSummary = phaseSummary,
            companion = companion,
            validation = validation,
            log = log,
            logText = logText,
            historySummary = RoastHistoryEngine.summary(),
            recentRoasts = RoastHistoryEngine.all().take(3)
        )

        lastSnapshot = snapshot
        return snapshot
    }

    fun stopAndSave(
        machineName: String = "HB M2SE"
    ): RoastHistorySaveResult {

        val session = RoastSessionEngine.currentState()

        val result = RoastHistoryEngine.saveCurrentRoastLog(
            session = session,
            machineName = machineName
        )

        MachineBridge.stop()

        val updatedSnapshot = tick()
        lastSnapshot = updatedSnapshot

        return result
    }

    fun startNewRoast() {
        MachineBridge.stop()
        reset()
        MachineBridge.start()
        tick()
    }
}
