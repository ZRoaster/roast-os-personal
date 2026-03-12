package com.roastos.app

data class RoastKnowledgePromotionRecord(
    val batchId: String,
    val sourceTag: String,
    val evolvedState: String,
    val promoted: Boolean,
    val promotedTag: String,
    val recommendation: String
)

object RoastKnowledgePromotionEngine {

    fun buildRecords(): List<RoastKnowledgePromotionRecord> {
        val evolutionRecords = RoastKnowledgeEvolutionEngine.buildRecords()

        return evolutionRecords.map { evolution ->

            val promoted = shouldPromote(evolution)
            val promotedTag = buildPromotedTag(evolution, promoted)
            val recommendation = buildRecommendation(evolution, promoted)

            RoastKnowledgePromotionRecord(
                batchId = evolution.batchId,
                sourceTag = evolution.sourceTag,
                evolvedState = evolution.evolvedState,
                promoted = promoted,
                promotedTag = promotedTag,
                recommendation = recommendation
            )
        }
    }

    fun summary(): String {
        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Knowledge Promotion

Count
0

Status
No promotion records yet
            """.trimIndent()
        }

        val promoted = records.count { it.promoted }
        val waiting = records.count { !it.promoted && it.evolvedState == "Knowledge Candidate" }
        val stable = records.count { it.promotedTag == "Stable Knowledge" }
        val risk = records.count { it.promotedTag == "Risk Knowledge" }

        return """
Knowledge Promotion

Total
${records.size}

Promoted
$promoted

Waiting Candidate
$waiting

Stable Knowledge
$stable

Risk Knowledge
$risk
        """.trimIndent()
    }

    fun latestText(): String {
        val latest = buildRecords().lastOrNull()
            ?: return """
Knowledge Promotion

No promotion record yet.
            """.trimIndent()

        return """
Batch
${latest.batchId}

Source Tag
${latest.sourceTag}

Evolved State
${latest.evolvedState}

Promoted
${if (latest.promoted) "Yes" else "No"}

Promoted Tag
${latest.promotedTag}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    private fun shouldPromote(
        evolution: RoastKnowledgeEvolutionRecord
    ): Boolean {
        return when (evolution.evolvedState) {
            "Stable Reference" -> true
            "Knowledge Candidate" -> evolution.confidence == "高" || evolution.confidence == "中"
            "Risk Memory" -> true
            else -> false
        }
    }

    private fun buildPromotedTag(
        evolution: RoastKnowledgeEvolutionRecord,
        promoted: Boolean
    ): String {
        if (!promoted) {
            return "Not Promoted"
        }

        return when (evolution.evolvedState) {
            "Stable Reference" -> "Stable Knowledge"
            "Knowledge Candidate" -> "Promoted Candidate"
            "Risk Memory" -> "Risk Knowledge"
            "Waiting Validation" -> "Pending Knowledge"
            else -> "Unknown"
        }
    }

    private fun buildRecommendation(
        evolution: RoastKnowledgeEvolutionRecord,
        promoted: Boolean
    ): String {
        return when {
            promoted && evolution.evolvedState == "Stable Reference" ->
                "该条目可直接作为正式稳定知识保留。"

            promoted && evolution.evolvedState == "Knowledge Candidate" ->
                "该条目已达到提升条件，可纳入正式知识库候选。"

            promoted && evolution.evolvedState == "Risk Memory" ->
                "该条目应进入风险知识库，供后续规避参考。"

            !promoted && evolution.evolvedState == "Knowledge Candidate" ->
                "该候选还需要更多复现或验证，暂不提升。"

            !promoted && evolution.evolvedState == "Waiting Validation" ->
                "该条目需先补齐验证信息，再决定是否提升。"

            else ->
                "当前无需提升。"
        }
    }
}
