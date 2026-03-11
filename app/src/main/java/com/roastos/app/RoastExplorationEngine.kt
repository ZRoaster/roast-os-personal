package com.roastos.app

data class RoastExplorationAssessment(
    val batchId: String,
    val riskEventCount: Int,
    val roastHealth: String,
    val hasEvaluation: Boolean,
    val explorationStatus: String,
    val confidence: String,
    val summary: String
)

object RoastExplorationEngine {

    fun assessBatch(
        batchId: String
    ): RoastExplorationAssessment {

        val history = RoastHistoryEngine.findByBatchId(batchId)
        val events = RoastRiskEventEngine.eventsForBatch(batchId)

        if (history == null) {
            return RoastExplorationAssessment(
                batchId = batchId,
                riskEventCount = events.size,
                roastHealth = "未知",
                hasEvaluation = false,
                explorationStatus = "无记录",
                confidence = "低",
                summary = "未找到该批次历史记录。"
            )
        }

        val evaluation = history.evaluation
        val hasEvaluation = evaluation != null

        val explorationStatus = when {
            events.isEmpty() -> "常规批次"
            !hasEvaluation -> "待验证"
            isLikelySuccessfulExploration(history) -> "可控探索"
            else -> "高风险探索"
        }

        val confidence = when {
            events.isEmpty() -> "高"
            !hasEvaluation -> "低"
            isLikelySuccessfulExploration(history) -> "中"
            else -> "中"
        }

        return RoastExplorationAssessment(
            batchId = batchId,
            riskEventCount = events.size,
            roastHealth = history.roastHealthHeadline,
            hasEvaluation = hasEvaluation,
            explorationStatus = explorationStatus,
            confidence = confidence,
            summary = buildSummary(
                history = history,
                eventCount = events.size,
                explorationStatus = explorationStatus,
                hasEvaluation = hasEvaluation
            )
        )
    }

    fun latestAssessment(): RoastExplorationAssessment? {
        val latest = RoastHistoryEngine.latest() ?: return null
        return assessBatch(latest.batchId)
    }

    fun buildDisplayText(
        batchId: String
    ): String {
        val result = assessBatch(batchId)

        return """
Batch
${result.batchId}

Risk Events
${result.riskEventCount}

Roast Health
${result.roastHealth}

Evaluation
${if (result.hasEvaluation) "已记录" else "未记录"}

Exploration
${result.explorationStatus}

Confidence
${result.confidence}

Summary
${result.summary}
        """.trimIndent()
    }

    private fun isLikelySuccessfulExploration(
        history: RoastHistoryEntry
    ): Boolean {
        val e = history.evaluation ?: return false

        val sweetnessOk = (e.sweetness ?: 0) >= 6
        val clarityOk = (e.flavorClarity ?: 0) >= 6
        val balanceOk = (e.balance ?: 0) >= 6

        val awOk = e.roastedAw?.let { it in 0.30..0.60 } ?: true
        val colorOk = e.beanColor?.let { it in 45.0..85.0 } ?: true

        return sweetnessOk && clarityOk && balanceOk && awOk && colorOk
    }

    private fun buildSummary(
        history: RoastHistoryEntry,
        eventCount: Int,
        explorationStatus: String,
        hasEvaluation: Boolean
    ): String {
        return when {
            eventCount == 0 -> "该批次未触发明显风险事件，可视为常规烘焙。"

            !hasEvaluation ->
                "该批次发生了风险探索，但还没有 aw / 色值 / 杯测结果，暂时不能判断是否属于可控风险。"

            explorationStatus == "可控探索" ->
                "该批次虽然触发了风险事件，但后续结果表现可接受，可纳入风险可控范围候选。"

            else ->
                "该批次触发了风险事件，且结果暂不支持将其视为可控探索，建议继续谨慎处理。"
        }
    }
}
