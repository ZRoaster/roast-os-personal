package com.roastos.app

data class RoastSessionBusSnapshot(

    val session: RoastSessionState,

    val companion: RoastCompanionMessage,

    val log: RoastLog,

    val phaseSummary: String,

    val logText: String,

    val historySummary: String,

    val validation: RoastValidationResult

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

        RoastPhaseDetectionEngine.update(session)

        RoastLogEngine.update(session)

        val companion = RoastCompanionEngine.buildMessage(session)

        val log = RoastLogEngine.buildLog(session)

        val validation = RoastSessionValidator.validate(
            RoastSessionBusSnapshot(
                session = session,
                companion = companion,
                log = log,
                phaseSummary = RoastPhaseDetectionEngine.summary(),
                logText = "",
                historySummary = "",
                validation = RoastValidationResult(emptyList())
            )
        )

        val snapshot = RoastSessionBusSnapshot(

            session = session,

            companion = companion,

            log = log,

            phaseSummary = RoastPhaseDetectionEngine.summary(),

            logText = RoastLogEngine.buildLogText(session),

            historySummary = RoastHistoryEngine.summary(),

            validation = validation
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
