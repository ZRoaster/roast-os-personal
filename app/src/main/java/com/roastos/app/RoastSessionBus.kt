package com.roastos.app

data class RoastSessionBusSnapshot(
    val session: RoastSessionState,
    val companion: RoastCompanionMessage,
    val log: RoastLog,
    val phaseSummary: String,
    val logText: String,
    val historySummary: String
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

    /**
     * 统一更新总线
     *
     * 正确顺序：
     * 1. 读取 Session
     * 2. 更新 PhaseDetection
     * 3. 更新 LogEngine
     * 4. 生成 Companion
     * 5. 生成 Snapshot
     */
    fun tick(): RoastSessionBusSnapshot {
        val session = RoastSessionEngine.currentState()

        RoastPhaseDetectionEngine.update(session)
        RoastLogEngine.update(session)

        val companion = RoastCompanionEngine.buildMessage(session)
        val log = RoastLogEngine.buildLog(session)

        val snapshot = RoastSessionBusSnapshot(
            session = session,
            companion = companion,
            log = log,
            phaseSummary = RoastPhaseDetectionEngine.summary(),
            logText = RoastLogEngine.buildLogText(session),
            historySummary = RoastHistoryEngine.summary()
        )

        lastSnapshot = snapshot
        return snapshot
    }

    /**
     * 结束当前烘焙并保存历史
     */
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

    /**
     * 启动新一锅
     */
    fun startNewRoast() {
        MachineBridge.stop()
        reset()
        MachineBridge.start()
        tick()
    }
}
