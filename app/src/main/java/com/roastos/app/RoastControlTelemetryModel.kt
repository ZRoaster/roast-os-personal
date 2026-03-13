package com.roastos.app

data class RoastControlTelemetrySnapshot(
    val currentBeanTemp: Double,
    val currentRor: Double,
    val phase: String,
    val elapsedSec: Int,
    val hasValidationIssue: Boolean,
    val highestSeverity: String,
    val topIssueCode: String?
) {
    fun summaryText(): String {
        return """
Control Telemetry

Bean Temp
${String.format("%.1f", currentBeanTemp)} ℃

RoR
${String.format("%.1f", currentRor)} ℃/min

Phase
$phase

Elapsed
${elapsedSec}s

Validation Issue
${if (hasValidationIssue) "Yes" else "No"}

Highest Severity
$highestSeverity

Top Issue
${topIssueCode ?: "-"}
        """.trimIndent()
    }
}

object RoastControlTelemetryModel {

    fun evaluate(
        snapshot: RoastSessionBusSnapshot
    ): RoastControlTelemetrySnapshot {
        val session = snapshot.session
        val topIssue = snapshot.validation.issues.firstOrNull()

        return RoastControlTelemetrySnapshot(
            currentBeanTemp = session.lastBeanTemp,
            currentRor = session.lastRor,
            phase = snapshot.companion.phaseLabel,
            elapsedSec = session.lastElapsedSec,
            hasValidationIssue = snapshot.validation.hasIssues(),
            highestSeverity = snapshot.validation.highestSeverity() ?: "none",
            topIssueCode = topIssue?.code
        )
    }
}
