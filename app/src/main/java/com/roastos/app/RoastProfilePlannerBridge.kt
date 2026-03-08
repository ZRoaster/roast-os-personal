package com.roastos.app

data class RoastProfilePlanSuggestion(
    val profileId: String,
    val sourceBatchId: String,
    val profileName: String,

    val suggestedTurningSec: Int?,
    val suggestedYellowSec: Int?,
    val suggestedFcSec: Int?,
    val suggestedDropSec: Int?,
    val suggestedDevSec: Int?,
    val suggestedDtrPercent: Double?,

    val replayability: String,
    val risk: String,
    val summary: String
)

object RoastProfilePlannerBridge {

    fun buildFromProfile(profileId: String): RoastProfilePlanSuggestion? {
        val profile = RoastProfileEngine.findByProfileId(profileId) ?: return null
        return buildFromProfile(profile)
    }

    fun buildFromProfile(profile: RoastProfile): RoastProfilePlanSuggestion {
        val turning = chooseAnchor(
            actual = profile.actualTurningSec,
            predicted = profile.predictedTurningSec
        )

        val yellow = chooseAnchor(
            actual = profile.actualYellowSec,
            predicted = profile.predictedYellowSec
        )

        val fc = chooseAnchor(
            actual = profile.actualFcSec,
            predicted = profile.predictedFcSec
        )

        val drop = chooseAnchor(
            actual = profile.actualDropSec,
            predicted = profile.predictedDropSec
        )

        val dev = if (fc != null && drop != null && drop > fc) {
            drop - fc
        } else {
            null
        }

        val dtr = if (fc != null && drop != null && drop > fc && drop > 0) {
            ((drop - fc).toDouble() / drop.toDouble()) * 100.0
        } else {
            null
        }

        val summary = """
Profile Planner Bridge

Profile
${profile.name}

Source Batch
${profile.sourceBatchId}

Replayability
${profile.replayability}

Risk
${profile.risk}

Suggested Anchors
Turning   ${formatSec(turning)}
Yellow    ${formatSec(yellow)}
FC        ${formatSec(fc)}
Drop      ${formatSec(drop)}

Suggested Development
Dev       ${dev?.let { "${it}s" } ?: "-"}
DTR       ${dtr?.let { "%.1f".format(it) + "%" } ?: "-"}

Bridge Logic
Preferred actual anchor when available
Fallback to predicted anchor when actual is missing
        """.trimIndent()

        return RoastProfilePlanSuggestion(
            profileId = profile.profileId,
            sourceBatchId = profile.sourceBatchId,
            profileName = profile.name,
            suggestedTurningSec = turning,
            suggestedYellowSec = yellow,
            suggestedFcSec = fc,
            suggestedDropSec = drop,
            suggestedDevSec = dev,
            suggestedDtrPercent = dtr,
            replayability = profile.replayability,
            risk = profile.risk,
            summary = summary
        )
    }

    private fun chooseAnchor(actual: Int?, predicted: Int?): Int? {
        return actual ?: predicted
    }

    private fun formatSec(value: Int?): String {
        return value?.toString()?.plus("s") ?: "-"
    }
}
