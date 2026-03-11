package com.roastos.app

data class RoastRiskEvent(
    val id: String,
    val batchId: String,

    val timestampMillis: Long,
    val elapsedSec: Int,

    val phase: String,

    val beanTemp: Double,
    val ror: Double,

    val issueCode: String,
    val severity: String,

    val suggestedHeatAction: String,
    val suggestedAirflowAction: String,

    val operatorContinued: Boolean,

    var beanColor: Double? = null,
    var groundColor: Double? = null,
    var aw: Double? = null,
    var cupScore: Int? = null,
    var cupNotes: String? = null
)

object RoastRiskEventEngine {

    private val events = mutableListOf<RoastRiskEvent>()

    fun recordFromSnapshot(
        snapshot: RoastSessionBusSnapshot,
        decision: RoastDecision
    ) {
        val validation = snapshot.validation

        if (!validation.hasIssues()) return

        val session = snapshot.session
        val phase = snapshot.companion.phaseLabel
        val batchId = snapshot.log.batchId

        validation.issues.forEach { issue ->

            val event = RoastRiskEvent(
                id = generateId(),
                batchId = batchId,

                timestampMillis = System.currentTimeMillis(),
                elapsedSec = session.lastElapsedSec,

                phase = phase,

                beanTemp = session.lastBeanTemp,
                ror = session.lastRor,

                issueCode = issue.code,
                severity = issue.severity,

                suggestedHeatAction = decision.heatAction,
                suggestedAirflowAction = decision.airflowAction,

                operatorContinued = true
            )

            events.add(event)
        }
    }

    fun attachCupResult(
        batchId: String,
        beanColor: Double?,
        groundColor: Double?,
        aw: Double?,
        cupScore: Int?,
        notes: String?
    ) {
        events
            .filter { event -> event.batchId == batchId }
            .forEach { event ->
                event.beanColor = beanColor
                event.groundColor = groundColor
                event.aw = aw
                event.cupScore = cupScore
                event.cupNotes = notes
            }
    }

    fun eventsForBatch(
        batchId: String
    ): List<RoastRiskEvent> {
        return events.filter { event -> event.batchId == batchId }
    }

    fun all(): List<RoastRiskEvent> {
        return events.toList()
    }

    fun summary(): String {

        if (events.isEmpty()) {
            return """
Risk Events

Count
0

Status
No recorded risk
            """.trimIndent()
        }

        val latest = events.last()

        return """
Risk Events

Total
${events.size}

Latest Issue
${latest.issueCode}

Phase
${latest.phase}

BT
${String.format("%.1f", latest.beanTemp)}

RoR
${String.format("%.1f", latest.ror)}

Override
${if (latest.operatorContinued) "Continued" else "Stopped"}
        """.trimIndent()
    }

    private fun generateId(): String {
        return "risk-${System.currentTimeMillis()}"
    }
}
