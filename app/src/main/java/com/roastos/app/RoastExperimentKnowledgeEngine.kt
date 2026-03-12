package com.roastos.app

data class RoastExperimentKnowledgeRecord(
    val batchId: String,
    val knowledgeTag: String,
    val recommendation: String
)

object RoastExperimentKnowledgeEngine {

    fun latestText(): String {

        val records = RoastExperimentLearningEngine.buildRecords()

        if (records.isEmpty()) {

            return """
Experiment Knowledge

No experiment records yet
            """.trimIndent()
        }

        val latest = records.last()

        val tag = when (latest.learningState) {

            "Routine" -> "Routine Reference"

            "Controllable" -> "Controllable Pattern"

            "Risky" -> "Risk Pattern"

            else -> "Pending Validation"
        }

        val recommendation = when (tag) {

            "Routine Reference" ->
                "该批次可作为稳定参考。"

            "Controllable Pattern" ->
                "该实验具备可控价值，可继续复现验证。"

            "Risk Pattern" ->
                "该实验存在明显风险，不建议复用。"

            else ->
                "建议继续补充杯测与验证。"
        }

        return """
Batch
${latest.batchId}

Learning State
${latest.learningState}

Knowledge Tag
$tag

Recommendation
$recommendation
        """.trimIndent()
    }
}
