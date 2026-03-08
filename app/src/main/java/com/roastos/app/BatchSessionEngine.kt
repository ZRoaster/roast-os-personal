package com.roastos.app

data class BeanProfileSnapshot(
    val process: String = "",
    val density: Double = 0.0,
    val moisture: Double = 0.0,
    val aw: Double = 0.0
)

data class EnvSnapshot(
    val tempC: Double = 0.0,
    val humidityRh: Double = 0.0,
    val pressureKpa: Double = 1013.0
)

data class PlannerSnapshot(
    val chargeTemp: Int = 0,
    val predictedTurningSec: Int? = null,
    val predictedYellowSec: Int? = null,
    val predictedFcSec: Int? = null,
    val predictedDropSec: Int? = null,
    val predictedDevSec: Int? = null,
    val predictedDtrPercent: Double? = null
)

data class BatchSession(
    val batchId: String,
    val startTimeMillis: Long,
    var endTimeMillis: Long? = null,

    var status: String = "Idle", // Idle / Running / Finished / Corrected

    var beanSnapshot: BeanProfileSnapshot = BeanProfileSnapshot(),
    var envSnapshot: EnvSnapshot = EnvSnapshot(),
    var plannerSnapshot: PlannerSnapshot = PlannerSnapshot(),

    var notes: String = ""
) {
    fun durationMillis(): Long? {
        val end = endTimeMillis ?: return null
        return end - startTimeMillis
    }

    fun isRunning(): Boolean = status == "Running"
    fun isFinished(): Boolean = status == "Finished" || status == "Corrected"
}

object BatchSessionEngine {

    private var currentSession: BatchSession? = null

    fun current(): BatchSession? = currentSession

    fun hasActiveSession(): Boolean {
        return currentSession?.isRunning() == true
    }

    fun startFromPlanner(): BatchSession {
        val planner = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput

        val now = System.currentTimeMillis()
        val batchId = generateBatchId(now)

        val predTurning = planner?.let { (it.h1Sec - 60.0).toInt().coerceAtLeast(50) }
        val predYellow = planner?.h2Sec?.toInt()
        val predFc = planner?.fcPredSec?.toInt()
        val predDrop = planner?.dropSec?.toInt()
        val predDev = if (predFc != null && predDrop != null && predDrop > predFc) predDrop - predFc else null
        val predDtr = if (predFc != null && predDrop != null && predDrop > 0 && predDrop > predFc) {
            ((predDrop - predFc).toDouble() / predDrop.toDouble()) * 100.0
        } else {
            null
        }

        val session = BatchSession(
            batchId = batchId,
            startTimeMillis = now,
            status = "Running",
            beanSnapshot = BeanProfileSnapshot(
                process = plannerInput?.process ?: "",
                density = plannerInput?.density ?: 0.0,
                moisture = plannerInput?.moisture ?: 0.0,
                aw = plannerInput?.aw ?: 0.0
            ),
            envSnapshot = EnvSnapshot(
                tempC = plannerInput?.envTemp ?: 0.0,
                humidityRh = plannerInput?.envRH ?: 0.0,
                pressureKpa = 1013.0
            ),
            plannerSnapshot = PlannerSnapshot(
                chargeTemp = planner?.chargeBT ?: 0,
                predictedTurningSec = predTurning,
                predictedYellowSec = predYellow,
                predictedFcSec = predFc,
                predictedDropSec = predDrop,
                predictedDevSec = predDev,
                predictedDtrPercent = predDtr
            )
        )

        currentSession = session

        RoastTimelineStore.syncPredicted(
            turningSec = predTurning,
            yellowSec = predYellow,
            fcSec = predFc,
            dropSec = predDrop
        )

        return session
    }

    fun startManual(
        process: String,
        density: Double,
        moisture: Double,
        aw: Double,
        tempC: Double,
        humidityRh: Double,
        chargeTemp: Int,
        predTurning: Int?,
        predYellow: Int?,
        predFc: Int?,
        predDrop: Int?
    ): BatchSession {
        val now = System.currentTimeMillis()
        val batchId = generateBatchId(now)

        val predDev = if (predFc != null && predDrop != null && predDrop > predFc) predDrop - predFc else null
        val predDtr = if (predFc != null && predDrop != null && predDrop > 0 && predDrop > predFc) {
            ((predDrop - predFc).toDouble() / predDrop.toDouble()) * 100.0
        } else {
            null
        }

        val session = BatchSession(
            batchId = batchId,
            startTimeMillis = now,
            status = "Running",
            beanSnapshot = BeanProfileSnapshot(
                process = process,
                density = density,
                moisture = moisture,
                aw = aw
            ),
            envSnapshot = EnvSnapshot(
                tempC = tempC,
                humidityRh = humidityRh,
                pressureKpa = 1013.0
            ),
            plannerSnapshot = PlannerSnapshot(
                chargeTemp = chargeTemp,
                predictedTurningSec = predTurning,
                predictedYellowSec = predYellow,
                predictedFcSec = predFc,
                predictedDropSec = predDrop,
                predictedDevSec = predDev,
                predictedDtrPercent = predDtr
            )
        )

        currentSession = session

        RoastTimelineStore.syncPredicted(
            turningSec = predTurning,
            yellowSec = predYellow,
            fcSec = predFc,
            dropSec = predDrop
        )

        return session
    }

    fun syncLiveData(
        turningSec: Int?,
        yellowSec: Int?,
        fcSec: Int?,
        dropSec: Int?,
        ror: Double?
    ) {
        if (currentSession == null) return

        RoastTimelineStore.syncActual(
            turningSec = turningSec,
            yellowSec = yellowSec,
            fcSec = fcSec,
            dropSec = dropSec,
            ror = ror
        )
    }

    fun finish(notes: String = ""): BatchSession? {
        val session = currentSession ?: return null
        session.endTimeMillis = System.currentTimeMillis()
        session.status = "Finished"
        if (notes.isNotBlank()) {
            session.notes = notes
        }
        return session
    }

    fun markCorrected(notes: String = ""): BatchSession? {
        val session = currentSession ?: return null
        if (session.endTimeMillis == null) {
            session.endTimeMillis = System.currentTimeMillis()
        }
        session.status = "Corrected"
        if (notes.isNotBlank()) {
            session.notes = notes
        }
        return session
    }

    fun appendNotes(extra: String) {
        val session = currentSession ?: return
        session.notes = if (session.notes.isBlank()) {
            extra
        } else {
            session.notes + "\n" + extra
        }
    }

    fun resetCurrentSession() {
        currentSession = null
        RoastTimelineStore.reset()
    }

    fun summary(): String {
        val session = currentSession ?: return """
Batch Session

No active session
        """.trimIndent()

        return """
Batch Session

Batch ID
${session.batchId}

Status
${session.status}

Started
${session.startTimeMillis}

Ended
${session.endTimeMillis?.toString() ?: "-"}

Bean
Process ${session.beanSnapshot.process}
Density ${"%.1f".format(session.beanSnapshot.density)}
Moisture ${"%.1f".format(session.beanSnapshot.moisture)}
aw ${"%.2f".format(session.beanSnapshot.aw)}

Environment
Temp ${"%.1f".format(session.envSnapshot.tempC)}℃
RH ${"%.1f".format(session.envSnapshot.humidityRh)}%

Planner Snapshot
Charge ${session.plannerSnapshot.chargeTemp}℃
Turning ${session.plannerSnapshot.predictedTurningSec?.toString() ?: "-"}
Yellow ${session.plannerSnapshot.predictedYellowSec?.toString() ?: "-"}
FC ${session.plannerSnapshot.predictedFcSec?.toString() ?: "-"}
Drop ${session.plannerSnapshot.predictedDropSec?.toString() ?: "-"}

Notes
${if (session.notes.isBlank()) "-" else session.notes}
        """.trimIndent()
    }

    private fun generateBatchId(now: Long): String {
        return "BATCH-$now"
    }
}
