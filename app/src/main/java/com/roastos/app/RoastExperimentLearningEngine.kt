package com.roastos.app

data class RoastExperimentLearningRecord(
    val batchId: String,
    val riskEvents: Int,
    val cupScore: Int?,
    val aw: Double?,
    val beanColor: Double?,
    val learningState: String,
    val conclusion: String
)

object RoastExperimentLearningEngine {

    fun buildRecords(): List<RoastExperimentLearningRecord> {

        val history = RoastHistoryEngine.all()

        return history.map { entry ->

            val riskEvents = RoastRiskEventEngine.eventsForBatch(entry.batchId).size
            val evaluation = entry.evaluation

            val cupScore = buildCupScore(evaluation)
            val aw = evaluation?.roastedAw
            val color = evaluation?.beanColor

            val state = classifyLearning(
                riskEvents,
                cupScore,
                aw,
                color
            )

            RoastExperimentLearningRecord(
                batchId = entry.batchId,
                riskEvents = riskEvents,
                cupScore = cupScore,
                aw = aw,
                beanColor = color,
                learningState = state,
                conclusion = buildConclusion(state)
            )
        }
    }

    fun summary(): String {

        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Experiment Learning

Count
0

Status
No learning data
            """.trimIndent()
        }

        val controllable = records.count { it.learningState == "Controllable" }
        val risky = records.count { it.learningState == "Risky" }
        val pending = records.count { it.learningState == "Pending" }

        return """
Experiment Learning

Total
${records.size}

Controllable
$controllable

Risky
$risky

Pending
$pending
        """.trimIndent()
    }

    fun latestText(): String {

        val latest = buildRecords().lastOrNull()
            ?: return """
Experiment Learning

No learning record yet.
            """.trimIndent()

        return """
Batch
${latest.batchId}

Risk Events
${latest.riskEvents}

Cup Score
${latest.cupScore ?: "-"}

Aw
${latest.aw ?: "-"}

Bean Color
${latest.beanColor ?: "-"}

Learning State
${latest.learningState}

Conclusion
${latest.conclusion}
        """.trimIndent()
    }

    private fun classifyLearning(
        riskEvents: Int,
        cupScore: Int?,
        aw: Double?,
        color: Double?
    ): String {

        if (riskEvents == 0) {
            return "Routine"
        }

        if (cupScore == null) {
            return "Pending"
        }

        val awOk = aw?.let { it in 0.30..0.60 } ?: true
        val colorOk = color?.let { it in 45.0..85.0 } ?: true
        val cupOk = cupScore >= 6

        return if (awOk && colorOk && cupOk) {
            "Controllable"
        } else {
            "Risky"
        }
    }

    private fun buildConclusion(
        state: String
    ): String {

        return when (state) {

            "Routine" ->
                "常规烘焙批次。"

            "Pending" ->
                "存在探索行为，等待杯测验证。"

            "Controllable" ->
                "该探索已进入可控经验范围。"

            "Risky" ->
                "该探索风险较高，不建议复用。"

            else ->
                "信息不足。"
        }
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
