package com.roastos.app

data class RoastKnowledgeRecord(

    val batchId: String,

    val explorationStatus: String,

    val roastHealth: String,

    val cupScore: Int?,

    val beanColor: Double?,

    val aw: Double?,

    val riskEvents: Int,

    val resultTag: String
)

object RoastKnowledgeEngine {

    fun buildKnowledge(): List<RoastKnowledgeRecord> {

        val history = RoastHistoryEngine.all()

        val list = mutableListOf<RoastKnowledgeRecord>()

        history.forEach { entry ->

            val risks =
                RoastRiskEventEngine.eventsForBatch(entry.batchId)

            val evaluation = entry.evaluation

            val roastHealth =
                evaluateRoastHealth(entry)

            val explorationStatus =
                RoastExplorationEngine.classify(entry.batchId)

            val resultTag =
                classifyResult(roastHealth, evaluation)

            list.add(

                RoastKnowledgeRecord(
                    batchId = entry.batchId,
                    explorationStatus = explorationStatus,
                    roastHealth = roastHealth,
                    cupScore = evaluation?.sweetness,
                    beanColor = evaluation?.beanColor,
                    aw = evaluation?.roastedAw,
                    riskEvents = risks.size,
                    resultTag = resultTag
                )
            )
        }

        return list
    }

    private fun evaluateRoastHealth(
        entry: RoastHistoryEntry
    ): String {

        val ror = entry.actualPreFcRor ?: return "Unknown"

        return when {

            ror < 2.0 -> "Crash Risk"

            ror > 10 -> "Flick Risk"

            else -> "Stable"
        }
    }

    private fun classifyResult(
        roastHealth: String,
        evaluation: RoastEvaluation?
    ): String {

        if (evaluation == null) {
            return "Unverified"
        }

        val score =
            listOfNotNull(
                evaluation.sweetness,
                evaluation.acidity,
                evaluation.body,
                evaluation.balance
            ).average()

        return when {

            score >= 7 -> "Successful Exploration"

            score >= 5 -> "Acceptable"

            else -> "Needs Adjustment"
        }
    }

    fun summary(): String {

        val records = buildKnowledge()

        if (records.isEmpty()) {
            return "No knowledge records yet."
        }

        val success =
            records.count { it.resultTag == "Successful Exploration" }

        val acceptable =
            records.count { it.resultTag == "Acceptable" }

        val failed =
            records.count { it.resultTag == "Needs Adjustment" }

        return """
Roast Knowledge

Successful Exploration
$success

Acceptable
$acceptable

Needs Adjustment
$failed
        """.trimIndent()
    }
}
