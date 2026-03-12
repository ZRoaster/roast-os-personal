package com.roastos.app

data class RoastKnowledgeEvolutionRecord(
    val batchId: String,
    val sourceTag: String,
    val evolvedState: String,
    val confidence: String,
    val recommendation: String
)

object RoastKnowledgeEvolutionEngine {

    fun buildRecords(): List<RoastKnowledgeEvolutionRecord> {
        val knowledgeText = RoastExperimentKnowledgeEngine.latestText()

        if (knowledgeText.contains("No knowledge record yet")) {
            return emptyList()
        }

        val latestBatchId = extractField(knowledgeText, "Batch")
        val latestTag = extractField(knowledgeText, "Knowledge Tag")

        val evolvedState = when (latestTag) {
            "Controllable Pattern" -> "Knowledge Candidate"
            "Routine Reference" -> "Stable Reference"
            "Risk Pattern" -> "Risk Memory"
            "Pending Validation" -> "Waiting Validation"
            else -> "Unknown"
        }

        val confidence = when (latestTag) {
            "Controllable Pattern" -> "中"
            "Routine Reference" -> "高"
            "Risk Pattern" -> "中"
            "Pending Validation" -> "低"
            else -> "低"
        }

        val recommendation = when (evolvedState) {
            "Knowledge Candidate" ->
                "该条目可进入正式知识库候选，建议继续复现验证。"

            "Stable Reference" ->
                "该条目可作为稳定参考知识保留。"

            "Risk Memory" ->
                "该条目应保留为风险记忆，避免直接复用。"

            "Waiting Validation" ->
                "该条目还不能进化为正式知识，需继续补齐验证。"

            else ->
                "当前信息不足。"
        }

        if (latestBatchId.isBlank()) {
            return emptyList()
        }

        return listOf(
            RoastKnowledgeEvolutionRecord(
                batchId = latestBatchId,
                sourceTag = latestTag,
                evolvedState = evolvedState,
                confidence = confidence,
                recommendation = recommendation
            )
        )
    }

    fun latestText(): String {
        val latest = buildRecords().lastOrNull()
            ?: return """
Knowledge Evolution

No evolution record yet.
            """.trimIndent()

        return """
Batch
${latest.batchId}

Source Tag
${latest.sourceTag}

Evolved State
${latest.evolvedState}

Confidence
${latest.confidence}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    fun summary(): String {
        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Knowledge Evolution

Count
0

Status
No evolution data
            """.trimIndent()
        }

        val stable = records.count { it.evolvedState == "Stable Reference" }
        val candidate = records.count { it.evolvedState == "Knowledge Candidate" }
        val risk = records.count { it.evolvedState == "Risk Memory" }
        val waiting = records.count { it.evolvedState == "Waiting Validation" }

        return """
Knowledge Evolution

Total
${records.size}

Stable Reference
$stable

Knowledge Candidate
$candidate

Risk Memory
$risk

Waiting Validation
$waiting
        """.trimIndent()
    }

    private fun extractField(
        text: String,
        label: String
    ): String {
        val lines = text.lines()

        for (i in lines.indices) {
            if (lines[i].trim() == label && i + 1 < lines.size) {
                return lines[i + 1].trim()
            }
        }

        return ""
    }
}
