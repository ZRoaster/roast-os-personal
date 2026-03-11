package com.roastos.app

data class RoastLearningRecord(
    val batchId: String,
    val explorationStatus: String,
    val roastHealth: String,
    val riskEventCount: Int,
    val hasEvaluation: Boolean,
    val recommendation: String
)

data class RoastLearningSummary(
    val totalBatches: Int,
    val exploredBatches: Int,
    val controllableExplorations: Int,
    val pendingValidation: Int,
    val summaryText: String
)

object RoastLearningEngine {

    fun buildRecords(): List<RoastLearningRecord> {
        return RoastHistoryEngine.all().map { entry ->
            val assessment = RoastExplorationEngine.assessBatch(entry.batchId)

            RoastLearningRecord(
                batchId = entry.batchId,
                explorationStatus = assessment.explorationStatus,
                roastHealth = assessment.roastHealth,
                riskEventCount = assessment.riskEventCount,
                hasEvaluation = assessment.hasEvaluation,
                recommendation = buildRecommendation(assessment)
            )
        }
    }

    fun buildSummary(): RoastLearningSummary {
        val records = buildRecords()

        val total = records.size
        val explored = records.count { it.explorationStatus != "常规批次" && it.explorationStatus != "无记录" }
        val controllable = records.count { it.explorationStatus == "可控探索" }
        val pending = records.count { it.explorationStatus == "待验证" }

        return RoastLearningSummary(
            totalBatches = total,
            exploredBatches = explored,
            controllableExplorations = controllable,
            pendingValidation = pending,
            summaryText = buildSummaryText(
                total = total,
                explored = explored,
                controllable = controllable,
                pending = pending
            )
        )
    }

    fun latestLearningText(): String {
        val latest = RoastHistoryEngine.latest()
            ?: return """
Learning

No roast history yet.
            """.trimIndent()

        val assessment = RoastExplorationEngine.assessBatch(latest.batchId)

        return """
Batch
${latest.batchId}

Exploration
${assessment.explorationStatus}

Risk Events
${assessment.riskEventCount}

Roast Health
${assessment.roastHealth}

Evaluation
${if (assessment.hasEvaluation) "已记录" else "未记录"}

Recommendation
${buildRecommendation(assessment)}
        """.trimIndent()
    }

    private fun buildRecommendation(
        assessment: RoastExplorationAssessment
    ): String {
        return when (assessment.explorationStatus) {
            "常规批次" -> "可作为稳定参考批次。"
            "待验证" -> "先补 aw / 色值 / 杯测，再决定是否纳入经验。"
            "可控探索" -> "可列入可控探索候选，后续继续验证复现性。"
            "高风险探索" -> "暂不建议直接复用，先继续谨慎验证。"
            else -> "信息不足，暂不归纳。"
        }
    }

    private fun buildSummaryText(
        total: Int,
        explored: Int,
        controllable: Int,
        pending: Int
    ): String {
        if (total == 0) {
            return """
Learning Summary

No roast data yet.
            """.trimIndent()
        }

        return """
Learning Summary

Total Batches
$total

Exploration Batches
$explored

Controllable Explorations
$controllable

Pending Validation
$pending
        """.trimIndent()
    }
}
