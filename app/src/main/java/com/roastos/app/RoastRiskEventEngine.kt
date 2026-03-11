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
        policyBundle: RoastRiskDecisionBundle
    ) {

        if (!policyBundle.hasRisk) return

        val session = snapshot.session
        val phase = snapshot.companion.phaseLabel

        policyBundle.policies.forEach { policy ->

            val event = RoastRiskEvent(
                id = generateId(),
                batchId = session.batchId,

                timestampMillis = System.currentTimeMillis(),
                elapsedSec = session.lastElapsedSec,

                phase = phase,

                beanTemp = session.lastBeanTemp,
                ror = session.lastRor,

                issueCode = policy.issueCode,
                severity = policy.level,

                suggestedHeatAction = policy.suggestedHeatAction,
                suggestedAirflowAction = policy.suggestedAirflowAction,

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
            .filter { it.batchId == batchId }
            .forEach {

                it.beanColor = beanColor
                it.groundColor = groundColor
                it.aw = aw
                it.cupScore = cupScore
                it.cupNotes = notes
            }
    }

    fun eventsForBatch(
        batchId: String
    ): List<RoastRiskEvent> {
        return events.filter { it.batchId == batchId }
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
