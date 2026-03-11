package com.roastos.app

data class RoastStyleLearningRecord(
    val styleId: String,
    val styleName: String,
    val supportingBatchIds: List<String>,
    val successfulCount: Int,
    val acceptableCount: Int,
    val unverifiedCount: Int,
    val confidence: String,
    val recommendation: String
)

object RoastStyleLearningEngine {

    fun buildRecords(): List<RoastStyleLearningRecord> {
        val myStyles = MyStyleEngine.all()
        if (myStyles.isEmpty()) return emptyList()

        val knowledge = RoastKnowledgeEngine.buildKnowledge()

        return myStyles.map { style ->
            val supporting = knowledge.filter { record ->
                isSupportingStyle(style, record)
            }

            val successful = supporting.count { it.resultTag == "Successful Exploration" }
            val acceptable = supporting.count { it.resultTag == "Acceptable" }
            val unverified = supporting.count { it.resultTag == "Unverified" }

            RoastStyleLearningRecord(
                styleId = style.id,
                styleName = style.name,
                supportingBatchIds = supporting.map { it.batchId },
                successfulCount = successful,
                acceptableCount = acceptable,
                unverifiedCount = unverified,
                confidence = buildConfidence(successful, acceptable, unverified),
                recommendation = buildRecommendation(successful, acceptable, unverified)
            )
        }
    }

    fun summary(): String {
        val records = buildRecords()

        if (records.isEmpty()) {
            return """
Style Learning

Count
0

Status
No learning records yet
            """.trimIndent()
        }

        val strong = records.count { it.confidence == "高" }
        val medium = records.count { it.confidence == "中" }
        val low = records.count { it.confidence == "低" }

        return """
Style Learning

Tracked Styles
${records.size}

High Confidence
$strong

Medium Confidence
$medium

Low Confidence
$low
        """.trimIndent()
    }

    fun latestText(): String {
        val latest = buildRecords().lastOrNull()
            ?: return """
Style Learning

No style learning record yet.
            """.trimIndent()

        return """
Style
${latest.styleName}

Supporting Batches
${latest.supportingBatchIds.size}

Successful
${latest.successfulCount}

Acceptable
${latest.acceptableCount}

Unverified
${latest.unverifiedCount}

Confidence
${latest.confidence}

Recommendation
${latest.recommendation}
        """.trimIndent()
    }

    private fun isSupportingStyle(
        style: RoastStyleProfile,
        record: RoastKnowledgeRecord
    ): Boolean {
        val batch = RoastHistoryEngine.findByBatchId(record.batchId) ?: return false

        val devRatio = buildDevelopmentRatio(batch)

        val styleDev = style.developmentRatio
        val styleProcess = style.suitableProcess

        val devMatch = when {
            styleDev == null || devRatio == null -> true
            else -> kotlin.math.abs(styleDev - devRatio) <= 0.04
        }

        val processMatch = when {
            styleProcess.isNullOrBlank() -> true
            styleProcess.equals("Any", ignoreCase = true) -> true
            else -> batch.process.contains(styleProcess, ignoreCase = true) ||
                styleProcess.contains(batch.process, ignoreCase = true)
        }

        return devMatch && processMatch
    }

    private fun buildDevelopmentRatio(
        entry: RoastHistoryEntry
    ): Double? {
        val drop = entry.actualDropSec ?: entry.predictedDropSec ?: return null
        val fc = entry.actualFcSec ?: entry.predictedFcSec ?: return null

        if (drop <= 0 || drop < fc) return null

        val dev = drop - fc
        return dev.toDouble() / drop.toDouble()
    }

    private fun buildConfidence(
        successful: Int,
        acceptable: Int,
        unverified: Int
    ): String {
        return when {
            successful >= 3 -> "高"
            successful + acceptable >= 2 -> "中"
            unverified >= 1 -> "低"
            else -> "低"
        }
    }

    private fun buildRecommendation(
        successful: Int,
        acceptable: Int,
        unverified: Int
    ): String {
        return when {
            successful >= 3 ->
                "该风格已有较强复现支撑，可作为稳定个人风格继续使用。"

            successful >= 1 && acceptable >= 1 ->
                "该风格已有初步支撑，建议继续复现并积累更多结果。"

            unverified >= 1 ->
                "该风格已有候选批次，但缺少足够验证，建议补齐评测。"

            else ->
                "当前支撑不足，暂不建议将其视为稳定风格。"
        }
    }
}
