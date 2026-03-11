package com.roastos.app

data class RoastExperimentRecord(
    val batchId: String,
    val riskEventCount: Int,
    val hasCupResult: Boolean,
    val cupScore: Int?,
    val beanColor: Double?,
    val aw: Double?,
    val experimentStatus: String,
    val recommendation: String
)

object RoastExperimentTracker {

    fun buildRecords(): List<RoastExperimentRecord> {
        val history = RoastHistoryEngine.all()

        return history.map { entry ->
            val riskEvents = RoastRiskEventEngine.eventsForBatch(entry.batchId)
            val evaluation = entry.evaluation

            val cupScore = buildCupScore(evaluation)
            val hasCupResult = hasCupResult(evaluation)

            val status = classifyExperiment(
                riskEventCount = riskEvents.size,
                hasCupResult = hasCupResult,
                cupScore = cupScore,
                aw = evaluation?.roastedAw,
                beanColor = evaluation?.beanColor
            )

            RoastExperimentRecord(
                batchId = entry.batchId,
                riskEventCount = riskEvents.size,
                hasCupResult = hasCupResult,
                cupScore = cupScore,
                beanColor = evaluation?.beanColor,
                aw = evaluation?.roastedAw,
                experimentStatus = status,
                recommendation = buildRecommendation(status)
            )
        }
    }

    fun latestText(): String {
        val latest = buildRecords().lastOrNull()
            ?: return """
Experiment Tracker

No experiment record yet.
            """.trimIndent()

        return """
Batch
${latest.batchId}

Risk Events
${latest.riskEventCount}

Cup Result
${if (latest.hasCupResult) "Yes" else "No"}

Cup Score
${latest.cupScore ?: "-"}

Bean Color
${latest.beanColor ?: "-"}

Aw
${latest.aw ?: "-"}

Status
${latest.experimentStatus}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    fun summary(): String {
        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Experiment Tracker

Count
0

Status
No experiment data
            """.trimIndent()
        }

        val controllable = records.count { it.experimentStatus == "Controllable" }
        val pending = records.count { it.experimentStatus == "Pending Validation" }
        val risky = records.count { it.experimentStatus == "Needs Caution" }
        val routine = records.count { it.experimentStatus == "Routine Batch" }

        return """
Experiment Tracker

Total
${records.size}

Routine
$routine

Controllable
$controllable

Pending Validation
$pending

Needs Caution
$risky
        """.trimIndent()
    }

    private fun classifyExperiment(
        riskEventCount: Int,
        hasCupResult: Boolean,
        cupScore: Int?,
        aw: Double?,
        beanColor: Double?
    ): String {
        if (riskEventCount == 0) {
            return "Routine Batch"
        }

        if (!hasCupResult) {
            return "Pending Validation"
        }

        val awOk = aw?.let { it in 0.30..0.60 } ?: true
        val colorOk = beanColor?.let { it in 45.0..85.0 } ?: true
        val scoreOk = (cupScore ?: 0) >= 6

        return if (awOk && colorOk && scoreOk) {
            "Controllable"
        } else {
            "Needs Caution"
        }
    }

    private fun buildRecommendation(
        status: String
    ): String {
        return when (status) {
            "Routine Batch" ->
                "这是常规批次，可作为稳定参考。"

            "Pending Validation" ->
                "该批次有探索行为，建议补齐 aw / 色值 / 杯测。"

            "Controllable" ->
                "该探索已初步进入可控范围，可继续复现验证。"

            "Needs Caution" ->
                "该探索暂不建议直接复用，先谨慎复盘。"

            else ->
                "信息不足。"
        }
    }

    private fun hasCupResult(
        evaluation: RoastEvaluation?
    ): Boolean {
        if (evaluation == null) return false

        return evaluation.beanColor != null ||
            evaluation.groundColor != null ||
            evaluation.roastedAw != null ||
            evaluation.sweetness != null ||
            evaluation.acidity != null ||
            evaluation.body != null ||
            evaluation.flavorClarity != null ||
            evaluation.balance != null ||
            evaluation.notes.isNotBlank()
    }

    private fun buildCupScore(
        evaluation: RoastEvaluation?
    ): Int? {
        if (evaluation == null) return null

        val values = listOfNotNull(
            evaluation.sweetness,
            evaluation.acidity,
            evaluation.body,
            evaluation.flavorClarity,
            evaluation.balance
        )

        if (values.isEmpty()) return null

        return values.sum() / values.size
    }
}
