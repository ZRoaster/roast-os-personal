package com.roastos.app

data class RoastExperimentKnowledgeRecord(
    val batchId: String,
    val riskEvents: Int,
    val cupScore: Int?,
    val aw: Double?,
    val beanColor: Double?,
    val knowledgeState: String,
    val knowledgeTag: String,
    val recommendation: String
)

object RoastExperimentKnowledgeEngine {

    fun buildRecords(): List<RoastExperimentKnowledgeRecord> {
        val learningRecords = RoastExperimentLearningEngine.buildRecords()

        return learningRecords.map { record ->
            val tag = buildKnowledgeTag(
                learningState = record.learningState,
                cupScore = record.cupScore,
                aw = record.aw,
                beanColor = record.beanColor
            )

            RoastExperimentKnowledgeRecord(
                batchId = record.batchId,
                riskEvents = record.riskEvents,
                cupScore = record.cupScore,
                aw = record.aw,
                beanColor = record.beanColor,
                knowledgeState = record.learningState,
                knowledgeTag = tag,
                recommendation = buildRecommendation(
                    learningState = record.learningState,
                    knowledgeTag = tag
                )
            )
        }
    }

    fun summary(): String {
        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Experiment Knowledge

Count
0

Status
No experiment knowledge yet
            """.trimIndent()
        }

        val controllable = records.count { it.knowledgeTag == "Controllable Pattern" }
        val promising = records.count { it.knowledgeTag == "Promising Candidate" }
        val risky = records.count { it.knowledgeTag == "Risk Pattern" }
        val routine = records.count { it.knowledgeTag == "Routine Reference" }
        val pending = records.count { it.knowledgeTag == "Pending Validation" }

        return """
Experiment Knowledge

Total
${records.size}

Routine Reference
$routine

Controllable Pattern
$controllable

Promising Candidate
$promising

Risk Pattern
$risky

Pending Validation
$pending
        """.trimIndent()
    }

    fun latestText(): String {
        val latest = buildRecords().lastOrNull()
            ?: return """
Experiment Knowledge

No knowledge record yet.
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

Knowledge State
${latest.knowledgeState}

Knowledge Tag
${latest.knowledgeTag}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    private fun buildKnowledgeTag(
        learningState: String,
        cupScore: Int?,
        aw: Double?,
        beanColor: Double?
    ): String {
        return when (learningState) {
            "Routine" -> "Routine Reference"
            "Pending" -> "Pending Validation"
            "Controllable" -> {
                val strongCup = (cupScore ?: 0) >= 7
                val awOk = aw?.let { it in 0.30..0.60 } ?: true
                val colorOk = beanColor?.let { it in 45.0..85.0 } ?: true

                if (strongCup && awOk && colorOk) {
                    "Controllable Pattern"
                } else {
                    "Promising Candidate"
                }
            }
            "Risky" -> "Risk Pattern"
            else -> "Unknown"
        }
    }

    private fun buildRecommendation(
        learningState: String,
        knowledgeTag: String
    ): String {
        return when (knowledgeTag) {
            "Routine Reference" ->
                "该批次可作为稳定参考批次。"

            "Pending Validation" ->
                "该批次已有实验行为，建议继续补齐杯测与色值验证。"

            "Controllable Pattern" ->
                "该实验已具备知识化价值，可纳入可控经验库。"

            "Promising Candidate" ->
                "该实验已有积极信号，建议继续复现验证。"

            "Risk Pattern" ->
                "该实验暂不建议复用，应作为风险模式保留。"

            else ->
                when (learningState) {
                    "Routine" -> "可作为常规参考。"
                    "Controllable" -> "建议继续复现。"
                    "Risky" -> "建议谨慎处理。"
                    else -> "信息不足。"
                }
        }
    }
}
