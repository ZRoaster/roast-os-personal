package com.roastos.app

data class PlannerBaseline(
    val source: String,
    val label: String,

    val turningSec: Int?,
    val yellowSec: Int?,
    val fcSec: Int?,
    val dropSec: Int?,

    val devSec: Int?,
    val dtrPercent: Double?,

    val sourceProfileId: String? = null,
    val sourceBatchId: String? = null
)

object PlannerBaselineStore {

    private var currentBaseline: PlannerBaseline? = null

    fun current(): PlannerBaseline? {
        return currentBaseline
    }

    fun hasBaseline(): Boolean {
        return currentBaseline != null
    }

    fun clear() {
        currentBaseline = null
    }

    fun setFromSuggestion(
        suggestion: RoastProfilePlanSuggestion
    ): PlannerBaseline {
        val baseline = PlannerBaseline(
            source = "Profile Suggestion",
            label = suggestion.profileName,
            turningSec = suggestion.suggestedTurningSec,
            yellowSec = suggestion.suggestedYellowSec,
            fcSec = suggestion.suggestedFcSec,
            dropSec = suggestion.suggestedDropSec,
            devSec = suggestion.suggestedDevSec,
            dtrPercent = suggestion.suggestedDtrPercent,
            sourceProfileId = suggestion.profileId,
            sourceBatchId = suggestion.sourceBatchId
        )

        currentBaseline = baseline
        return baseline
    }

    fun setFromCurrentPlannerResult(): PlannerBaseline? {
        val planner = AppState.lastPlannerResult ?: return null

        val turning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val yellow = planner.h2Sec.toInt()
        val fc = planner.fcPredSec.toInt()
        val drop = planner.dropSec.toInt()

        val baseline = PlannerBaseline(
            source = "Current Planner Result",
            label = "Live Planner Baseline",
            turningSec = turning,
            yellowSec = yellow,
            fcSec = fc,
            dropSec = drop,
            devSec = planner.devTime,
            dtrPercent = planner.dtrPercent,
            sourceProfileId = null,
            sourceBatchId = null
        )

        currentBaseline = baseline
        return baseline
    }

    fun summary(): String {
        val baseline = currentBaseline ?: return """
Planner Baseline

Status
Not set

Next Step
Apply a profile suggestion
or capture current planner result as baseline
        """.trimIndent()

        return """
Planner Baseline

Source
${baseline.source}

Label
${baseline.label}

Turning
${baseline.turningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Yellow
${baseline.yellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

FC
${baseline.fcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Drop
${baseline.dropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Dev
${baseline.devSec?.let { "${it}s" } ?: "-"}

DTR
${baseline.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}
        """.trimIndent()
    }
}
