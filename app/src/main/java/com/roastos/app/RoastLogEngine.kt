package com.roastos.app

import kotlin.math.roundToInt

data class RoastLog(
    val batchId: String,
    val machineName: String,
    val status: String,
    val totalTimeSec: Int,
    val chargeTemp: Double?,
    val dropTemp: Double?,
    val turningPointSec: Int?,
    val turningPointTemp: Double?,
    val dryEndSec: Int?,
    val dryEndTemp: Double?,
    val maillardStartSec: Int?,
    val maillardStartTemp: Double?,
    val firstCrackSec: Int?,
    val firstCrackTemp: Double?,
    val dropSec: Int?,
    val developmentTimeSec: Int?,
    val developmentRatio: Double?,
    val finalRor: Double?,
    val summary: String
)

object RoastLogEngine {

    private var activeBatchId: String = newBatchId()
    private var chargeTemp: Double? = null
    private var dropTemp: Double? = null
    private var finalRor: Double? = null

    fun reset() {
        activeBatchId = newBatchId()
        chargeTemp = null
        dropTemp = null
        finalRor = null
    }

    fun update(
        session: RoastSessionState,
        machineName: String = "HB M2SE"
    ) {
        if (session.status == RoastSessionStatus.RUNNING) {
            if (chargeTemp == null && session.lastBeanTemp > 0.0) {
                chargeTemp = session.lastBeanTemp
            }

            dropTemp = session.lastBeanTemp
            finalRor = session.lastRor
        }
    }

    fun buildLog(
        session: RoastSessionState,
        machineName: String = "HB M2SE"
    ): RoastLog {
        val phaseState = RoastPhaseDetectionEngine.currentState()

        val firstCrackSec = phaseState.firstCrack?.elapsedSec
        val dropSec = phaseState.drop?.elapsedSec ?: session.lastElapsedSec

        val developmentTimeSec =
            if (firstCrackSec != null && dropSec >= firstCrackSec) {
                dropSec - firstCrackSec
            } else {
                null
            }

        val developmentRatio =
            if (developmentTimeSec != null && dropSec > 0) {
                developmentTimeSec.toDouble() / dropSec.toDouble()
            } else {
                null
            }

        val summary = buildSummary(
            machineName = machineName,
            session = session,
            phaseState = phaseState,
            developmentTimeSec = developmentTimeSec,
            developmentRatio = developmentRatio,
            chargeTemp = chargeTemp,
            dropTemp = dropTemp,
            finalRor = finalRor
        )

        return RoastLog(
            batchId = activeBatchId,
            machineName = machineName,
            status = session.status.name,
            totalTimeSec = session.lastElapsedSec,
            chargeTemp = chargeTemp,
            dropTemp = dropTemp,
            turningPointSec = phaseState.turningPoint?.elapsedSec,
            turningPointTemp = phaseState.turningPoint?.beanTemp,
            dryEndSec = phaseState.dryEnd?.elapsedSec,
            dryEndTemp = phaseState.dryEnd?.beanTemp,
            maillardStartSec = phaseState.maillardStart?.elapsedSec,
            maillardStartTemp = phaseState.maillardStart?.beanTemp,
            firstCrackSec = phaseState.firstCrack?.elapsedSec,
            firstCrackTemp = phaseState.firstCrack?.beanTemp,
            dropSec = phaseState.drop?.elapsedSec ?: session.lastElapsedSec,
            developmentTimeSec = developmentTimeSec,
            developmentRatio = developmentRatio,
            finalRor = finalRor,
            summary = summary
        )
    }

    fun buildLogText(
        session: RoastSessionState,
        machineName: String = "HB M2SE"
    ): String {
        val log = buildLog(session, machineName)

        return buildString {
            appendLine("ROAST LOG")
            appendLine()
            appendLine("Batch ID")
            appendLine(log.batchId)
            appendLine()
            appendLine("Machine")
            appendLine(log.machineName)
            appendLine()
            appendLine("Status")
            appendLine(log.status)
            appendLine()
            appendLine("Total Time")
            appendLine(formatSec(log.totalTimeSec))
            appendLine()
            appendLine("Charge Temp")
            appendLine(formatTemp(log.chargeTemp))
            appendLine()
            appendLine("Turning Point")
            appendLine(formatEvent(log.turningPointSec, log.turningPointTemp))
            appendLine()
            appendLine("Dry End")
            appendLine(formatEvent(log.dryEndSec, log.dryEndTemp))
            appendLine()
            appendLine("Maillard Start")
            appendLine(formatEvent(log.maillardStartSec, log.maillardStartTemp))
            appendLine()
            appendLine("First Crack")
            appendLine(formatEvent(log.firstCrackSec, log.firstCrackTemp))
            appendLine()
            appendLine("Drop")
            appendLine(
                if (log.dropSec == null) "-" else formatSec(log.dropSec)
            )
            appendLine()
            appendLine("Drop Temp")
            appendLine(formatTemp(log.dropTemp))
            appendLine()
            appendLine("Development Time")
            appendLine(
                if (log.developmentTimeSec == null) "-" else formatSec(log.developmentTimeSec)
            )
            appendLine()
            appendLine("Development Ratio")
            appendLine(
                if (log.developmentRatio == null) {
                    "-"
                } else {
                    "${((log.developmentRatio * 1000.0).roundToInt() / 10.0)}%"
                }
            )
            appendLine()
            appendLine("Final RoR")
            appendLine(
                if (log.finalRor == null) "-" else "${oneDecimal(log.finalRor)} ℃/min"
            )
            appendLine()
            appendLine("Summary")
            append(log.summary)
        }
    }

    private fun buildSummary(
        machineName: String,
        session: RoastSessionState,
        phaseState: RoastPhaseDetectionState,
        developmentTimeSec: Int?,
        developmentRatio: Double?,
        chargeTemp: Double?,
        dropTemp: Double?,
        finalRor: Double?
    ): String {
        return buildString {
            appendLine("Machine: $machineName")
            appendLine("Session: ${session.status.name}")
            appendLine("Elapsed: ${formatSec(session.lastElapsedSec)}")
            appendLine("Charge Temp: ${formatTemp(chargeTemp)}")
            appendLine("Turning Point: ${formatEvent(phaseState.turningPoint?.elapsedSec, phaseState.turningPoint?.beanTemp)}")
            appendLine("Dry End: ${formatEvent(phaseState.dryEnd?.elapsedSec, phaseState.dryEnd?.beanTemp)}")
            appendLine("Maillard Start: ${formatEvent(phaseState.maillardStart?.elapsedSec, phaseState.maillardStart?.beanTemp)}")
            appendLine("First Crack: ${formatEvent(phaseState.firstCrack?.elapsedSec, phaseState.firstCrack?.beanTemp)}")
            appendLine("Drop Temp: ${formatTemp(dropTemp)}")
            appendLine(
                "Development: ${
                    if (developmentTimeSec == null) "-" else formatSec(developmentTimeSec)
                }"
            )
            appendLine(
                "Development Ratio: ${
                    if (developmentRatio == null) "-" else "${((developmentRatio * 1000.0).roundToInt() / 10.0)}%"
                }"
            )
            append(
                "Final RoR: ${
                    if (finalRor == null) "-" else "${oneDecimal(finalRor)} ℃/min"
                }"
            )
        }
    }

    private fun formatEvent(
        sec: Int?,
        temp: Double?
    ): String {
        return if (sec == null || temp == null) {
            "-"
        } else {
            "${formatSec(sec)} · ${formatTemp(temp)}"
        }
    }

    private fun formatTemp(value: Double?): String {
        return if (value == null) "-" else "${oneDecimal(value)} ℃"
    }

    private fun formatSec(sec: Int): String {
        val minutes = sec / 60
        val seconds = sec % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun oneDecimal(value: Double): String {
        return "%.1f".format(value)
    }

    private fun newBatchId(): String {
        return "BATCH-${System.currentTimeMillis()}"
    }
}
