package com.roastos.app

data class RoastStyleEvolutionCandidate(
    val name: String,
    val sourceStyleIds: List<String>,
    val sourceBatchIds: List<String>,
    val flavorDirection: String,
    val developmentRatioRange: String,
    val rorTrend: String,
    val confidence: String,
    val recommendation: String
)

object RoastStyleEvolutionEngine {

    fun buildCandidates(): List<RoastStyleEvolutionCandidate> {
        val styles = MyStyleEngine.all()
        val learning = RoastStyleLearningEngine.buildRecords()

        if (styles.isEmpty() || learning.isEmpty()) {
            return emptyList()
        }

        val grouped = styles.groupBy { style ->
            buildClusterKey(style)
        }

        return grouped.mapNotNull { (_, groupStyles) ->
            if (groupStyles.isEmpty()) return@mapNotNull null

            val relatedLearning = learning.filter { record ->
                groupStyles.any { it.id == record.styleId }
            }

            if (relatedLearning.isEmpty()) return@mapNotNull null

            val sourceStyleIds = groupStyles.map { it.id }
            val sourceBatchIds = relatedLearning.flatMap { it.supportingBatchIds }.distinct()

            val flavorDirection = summarizeFlavorGoal(groupStyles)
            val devRange = summarizeDevelopment(groupStyles)
            val rorTrend = summarizeRorTrend(groupStyles)
            val confidence = summarizeConfidence(relatedLearning)
            val recommendation = buildRecommendation(relatedLearning, sourceBatchIds.size)

            RoastStyleEvolutionCandidate(
                name = buildCandidateName(groupStyles),
                sourceStyleIds = sourceStyleIds,
                sourceBatchIds = sourceBatchIds,
                flavorDirection = flavorDirection,
                developmentRatioRange = devRange,
                rorTrend = rorTrend,
                confidence = confidence,
                recommendation = recommendation
            )
        }
    }

    fun summary(): String {
        val candidates = buildCandidates()

        if (candidates.isEmpty()) {
            return """
Style Evolution

Count
0

Status
No evolution candidates yet
            """.trimIndent()
        }

        val high = candidates.count { it.confidence == "高" }
        val medium = candidates.count { it.confidence == "中" }
        val low = candidates.count { it.confidence == "低" }

        return """
Style Evolution

Candidates
${candidates.size}

High Confidence
$high

Medium Confidence
$medium

Low Confidence
$low
        """.trimIndent()
    }

    fun latestText(): String {
        val latest = buildCandidates().lastOrNull()
            ?: return """
Style Evolution

No evolution candidate yet.
            """.trimIndent()

        return """
Candidate
${latest.name}

Source Styles
${latest.sourceStyleIds.size}

Source Batches
${latest.sourceBatchIds.size}

Flavor Direction
${latest.flavorDirection}

Development Range
${latest.developmentRatioRange}

RoR Trend
${latest.rorTrend}

Confidence
${latest.confidence}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    private fun buildClusterKey(
        style: RoastStyleProfile
    ): String {
        val devBand = when (style.developmentRatio) {
            null -> "unknown"
            in 0.0..0.11 -> "short"
            in 0.11..0.15 -> "medium"
            else -> "long"
        }

        val process = style.suitableProcess?.lowercase()?.trim().orEmpty()
        val ror = style.rorTrend?.lowercase()?.trim().orEmpty()

        return "$devBand|$process|$ror"
    }

    private fun buildCandidateName(
        styles: List<RoastStyleProfile>
    ): String {
        val names = styles.map { it.name }
        val common = when {
            names.any { it.contains("Sweet", ignoreCase = true) } -> "Sweet"
            names.any { it.contains("Clarity", ignoreCase = true) } -> "Clarity"
            names.any { it.contains("Body", ignoreCase = true) } -> "Body"
            else -> "Hybrid"
        }

        return "$common Evolution"
    }

    private fun summarizeFlavorGoal(
        styles: List<RoastStyleProfile>
    ): String {
        val goals = styles.map { it.flavorGoal }.filter { it.isNotBlank() }

        if (goals.isEmpty()) return "Unknown"

        val text = goals.joinToString(" / ")

        return when {
            text.contains("甜", ignoreCase = true) && text.contains("清晰", ignoreCase = true) ->
                "甜感 / 清晰度"

            text.contains("甜", ignoreCase = true) ->
                "甜感导向"

            text.contains("酸", ignoreCase = true) || text.contains("bright", ignoreCase = true) ->
                "明亮导向"

            text.contains("body", ignoreCase = true) || text.contains("醇厚", ignoreCase = true) ->
                "醇厚导向"

            else ->
                goals.first()
        }
    }

    private fun summarizeDevelopment(
        styles: List<RoastStyleProfile>
    ): String {
        val values = styles.mapNotNull { it.developmentRatio }
        if (values.isEmpty()) return "-"

        val min = values.minOrNull() ?: return "-"
        val max = values.maxOrNull() ?: return "-"

        return "${String.format("%.1f", min * 100)}% - ${String.format("%.1f", max * 100)}%"
    }

    private fun summarizeRorTrend(
        styles: List<RoastStyleProfile>
    ): String {
        val trends = styles.mapNotNull { it.rorTrend }.filter { it.isNotBlank() }
        if (trends.isEmpty()) return "-"

        return trends
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: "-"
    }

    private fun summarizeConfidence(
        records: List<RoastStyleLearningRecord>
    ): String {
        val score = records.sumOf {
            when (it.confidence) {
                "高" -> 3
                "中" -> 2
                else -> 1
            }
        }

        return when {
            score >= 8 -> "高"
            score >= 4 -> "中"
            else -> "低"
        }
    }

    private fun buildRecommendation(
        records: List<RoastStyleLearningRecord>,
        batchCount: Int
    ): String {
        val high = records.count { it.confidence == "高" }
        val medium = records.count { it.confidence == "中" }

        return when {
            high >= 2 && batchCount >= 3 ->
                "该候选已具备较强支撑，值得整理成正式个人风格。"

            high >= 1 || medium >= 2 ->
                "该候选已有进化趋势，建议继续复现并补齐更多评测。"

            else ->
                "当前还像早期风格苗头，建议继续积累样本。"
        }
    }
}
