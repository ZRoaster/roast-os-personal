package com.roastos.app

data class RoastProfile(
    val profileId: String,
    val sourceBatchId: String,
    val createdAtMillis: Long,
    val name: String,
    val note: String,

    val process: String,
    val density: Double,
    val moisture: Double,
    val aw: Double,

    val envTemp: Double,
    val envRh: Double,

    val predictedTurningSec: Int?,
    val predictedYellowSec: Int?,
    val predictedFcSec: Int?,
    val predictedDropSec: Int?,

    val actualTurningSec: Int?,
    val actualYellowSec: Int?,
    val actualFcSec: Int?,
    val actualDropSec: Int?,
    val actualPreFcRor: Double?,

    val replayability: String,
    val risk: String,
    val evaluationSaved: Boolean
)

data class RoastProfileSaveResult(
    val saved: Boolean,
    val replacedExisting: Boolean,
    val totalCount: Int,
    val message: String
)

data class RoastProfileDeleteResult(
    val deleted: Boolean,
    val totalCount: Int,
    val message: String
)

object RoastProfileEngine {

    private const val MAX_PROFILE_COUNT = 100

    private val profiles = mutableListOf<RoastProfile>()

    fun all(): List<RoastProfile> {
        return profiles.sortedByDescending { it.createdAtMillis }
    }

    fun count(): Int {
        return profiles.size
    }

    fun latest(): RoastProfile? {
        return profiles.maxByOrNull { it.createdAtMillis }
    }

    fun findByProfileId(profileId: String): RoastProfile? {
        return profiles.firstOrNull { it.profileId == profileId }
    }

    fun findBySourceBatchId(batchId: String): RoastProfile? {
        return profiles.firstOrNull { it.sourceBatchId == batchId }
    }

    fun saveFromBatch(
        batchId: String,
        customName: String? = null,
        customNote: String = ""
    ): RoastProfileSaveResult {
        val entry = RoastHistoryEngine.findByBatchId(batchId)
            ?: return RoastProfileSaveResult(
                saved = false,
                replacedExisting = false,
                totalCount = profiles.size,
                message = "No roast history found for $batchId"
            )

        val replayability = buildReplayability(entry)
        val risk = buildRisk(entry)

        val profileId = "PROFILE-$batchId"
        val now = System.currentTimeMillis()

        val profile = RoastProfile(
            profileId = profileId,
            sourceBatchId = entry.batchId,
            createdAtMillis = now,
            name = customName?.takeIf { it.isNotBlank() }
                ?: buildDefaultName(entry),
            note = customNote,

            process = entry.process,
            density = entry.density,
            moisture = entry.moisture,
            aw = entry.aw,

            envTemp = entry.envTemp,
            envRh = entry.envRh,

            predictedTurningSec = entry.predictedTurningSec,
            predictedYellowSec = entry.predictedYellowSec,
            predictedFcSec = entry.predictedFcSec,
            predictedDropSec = entry.predictedDropSec,

            actualTurningSec = entry.actualTurningSec,
            actualYellowSec = entry.actualYellowSec,
            actualFcSec = entry.actualFcSec,
            actualDropSec = entry.actualDropSec,
            actualPreFcRor = entry.actualPreFcRor,

            replayability = replayability,
            risk = risk,
            evaluationSaved = entry.evaluation != null
        )

        val existingIndex = profiles.indexOfFirst { it.profileId == profileId }
        val replacedExisting = existingIndex >= 0

        if (existingIndex >= 0) {
            profiles[existingIndex] = profile
        } else {
            profiles.add(profile)
        }

        trimToMaxSize()

        return RoastProfileSaveResult(
            saved = true,
            replacedExisting = replacedExisting,
            totalCount = profiles.size,
            message = if (replacedExisting) {
                "Profile updated from $batchId"
            } else {
                "Profile saved from $batchId"
            }
        )
    }

    fun delete(profileId: String): RoastProfileDeleteResult {
        val removed = profiles.removeAll { it.profileId == profileId }

        return RoastProfileDeleteResult(
            deleted = removed,
            totalCount = profiles.size,
            message = if (removed) {
                "Deleted profile $profileId"
            } else {
                "No profile found for $profileId"
            }
        )
    }

    fun clear(): RoastProfileDeleteResult {
        profiles.clear()
        return RoastProfileDeleteResult(
            deleted = true,
            totalCount = 0,
            message = "All roast profiles cleared"
        )
    }

    fun summary(): String {
        if (profiles.isEmpty()) {
            return """
Roast Profile Library

Count
0

Latest
-

Status
Empty
            """.trimIndent()
        }

        val latest = latest()

        return """
Roast Profile Library

Count
${profiles.size}

Latest Profile
${latest?.name ?: "-"}

Source Batch
${latest?.sourceBatchId ?: "-"}

Replayability
${latest?.replayability ?: "-"}

Evaluation
${if (latest?.evaluationSaved == true) "Saved" else "Not saved"}
        """.trimIndent()
    }

    private fun buildDefaultName(entry: RoastHistoryEntry): String {
        val process = entry.process.ifBlank { "Roast" }
        val replayability = buildReplayability(entry)
        return "$process Profile ($replayability)"
    }

    private fun buildReplayability(entry: RoastHistoryEntry): String {
        val score = replayabilityScore(entry)
        return when {
            score >= 85 -> "High"
            score >= 65 -> "Medium"
            else -> "Low"
        }
    }

    private fun buildRisk(entry: RoastHistoryEntry): String {
        val score = riskScore(entry)
        return when {
            score >= 8 -> "High"
            score >= 4 -> "Medium"
            score >= 1 -> "Low"
            else -> "Minor"
        }
    }

    private fun replayabilityScore(entry: RoastHistoryEntry): Int {
        var score = 100

        val turningDelta = absDelta(entry.predictedTurningSec, entry.actualTurningSec)
        val yellowDelta = absDelta(entry.predictedYellowSec, entry.actualYellowSec)
        val fcDelta = absDelta(entry.predictedFcSec, entry.actualFcSec)
        val dropDelta = absDelta(entry.predictedDropSec, entry.actualDropSec)
        val ror = entry.actualPreFcRor

        score -= penalty(turningDelta, 2, 6, 12)
        score -= penalty(yellowDelta, 2, 8, 15)
        score -= penalty(fcDelta, 2, 10, 20)
        score -= penalty(dropDelta, 1, 10, 20)

        if (ror != null) {
            if (ror >= 10.8) score -= 18
            else if (ror >= 9.5) score -= 10
            else if (ror <= 7.0) score -= 18
            else if (ror <= 8.0) score -= 10
        }

        return score.coerceIn(0, 100)
    }

    private fun riskScore(entry: RoastHistoryEntry): Int {
        var score = 0

        val turningDelta = absDelta(entry.predictedTurningSec, entry.actualTurningSec)
        val yellowDelta = absDelta(entry.predictedYellowSec, entry.actualYellowSec)
        val fcDelta = absDelta(entry.predictedFcSec, entry.actualFcSec)
        val dropDelta = absDelta(entry.predictedDropSec, entry.actualDropSec)
        val ror = entry.actualPreFcRor

        if (turningDelta >= 12) score += 2 else if (turningDelta >= 6) score += 1
        if (yellowDelta >= 15) score += 2 else if (yellowDelta >= 8) score += 1
        if (fcDelta >= 20) score += 3 else if (fcDelta >= 10) score += 1
        if (dropDelta >= 20) score += 1 else if (dropDelta >= 10) score += 1

        if (ror != null) {
            if (ror >= 10.8 || ror <= 7.0) score += 3
            else if (ror >= 9.5 || ror <= 8.0) score += 1
        }

        return score
    }

    private fun penalty(absDelta: Int, mild: Int, mid: Int, high: Int): Int {
        return when {
            absDelta >= high -> 20
            absDelta >= mid -> 12
            absDelta >= mild -> 5
            else -> 0
        }
    }

    private fun absDelta(predicted: Int?, actual: Int?): Int {
        return if (predicted == null || actual == null) 0 else kotlin.math.abs(actual - predicted)
    }

    private fun trimToMaxSize() {
        if (profiles.size <= MAX_PROFILE_COUNT) return

        val sorted = profiles.sortedByDescending { it.createdAtMillis }
        val trimmed = sorted.take(MAX_PROFILE_COUNT)

        profiles.clear()
        profiles.addAll(trimmed)
    }
}
