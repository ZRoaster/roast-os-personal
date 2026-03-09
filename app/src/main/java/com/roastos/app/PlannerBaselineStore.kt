package com.roastos.app

import kotlin.math.abs

enum class BaselineMatchGrade {
    EXACT_MATCH,
    SIMILAR_MATCH,
    REFERENCE_ONLY
}

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
    val sourceBatchId: String? = null,

    val beanProcess: String? = null,
    val beanDensity: Double? = null,
    val beanMoisture: Double? = null,
    val beanAw: Double? = null,
    val roastLevel: String? = null,
    val orientation: String? = null
)

data class PlannerBaselineMatchResult(
    val grade: BaselineMatchGrade,
    val score: Int,
    val reasons: List<String>,
    val summary: String
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
        val profile = RoastProfileEngine.findByProfileId(suggestion.profileId)

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
            sourceBatchId = suggestion.sourceBatchId,
            beanProcess = profile?.process,
            beanDensity = profile?.density,
            beanMoisture = profile?.moisture,
            beanAw = profile?.aw,
            roastLevel = null,
            orientation = null
        )

        currentBaseline = baseline
        return baseline
    }

    fun setFromCurrentPlannerResult(): PlannerBaseline? {
        val planner = AppState.lastPlannerResult ?: return null
        val input = AppState.lastPlannerInput

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
            sourceBatchId = null,
            beanProcess = input?.process,
            beanDensity = input?.density,
            beanMoisture = input?.moisture,
            beanAw = input?.aw,
            roastLevel = input?.roastLevel,
            orientation = input?.orientation
        )

        currentBaseline = baseline
        return baseline
    }

    fun evaluateMatchAgainstCurrentInput(): PlannerBaselineMatchResult? {
        val baseline = currentBaseline ?: return null
        val input = AppState.lastPlannerInput ?: return null

        val reasons = mutableListOf<String>()
        var score = 0

        val baselineProcess = baseline.beanProcess.orEmpty()
        val inputProcess = input.process.orEmpty()

        if (baselineProcess.equals(inputProcess, ignoreCase = true) && baselineProcess.isNotBlank()) {
            score += 35
            reasons.add("Process matches current bean")
        } else if (
            baselineProcess.isNotBlank() &&
            inputProcess.isNotBlank() &&
            processFamily(baselineProcess) == processFamily(inputProcess)
        ) {
            score += 20
            reasons.add("Process family is similar")
        } else {
            reasons.add("Process differs")
        }

        val densityDelta = compareAbs(baseline.beanDensity, input.density)
        when {
            densityDelta == null -> reasons.add("Density comparison unavailable")
            densityDelta <= 15.0 -> {
                score += 25
                reasons.add("Density is very close")
            }
            densityDelta <= 35.0 -> {
                score += 15
                reasons.add("Density is reasonably close")
            }
            else -> reasons.add("Density differs noticeably")
        }

        val moistureDelta = compareAbs(baseline.beanMoisture, input.moisture)
        when {
            moistureDelta == null -> reasons.add("Moisture comparison unavailable")
            moistureDelta <= 0.3 -> {
                score += 15
                reasons.add("Moisture is very close")
            }
            moistureDelta <= 0.7 -> {
                score += 8
                reasons.add("Moisture is moderately close")
            }
            else -> reasons.add("Moisture differs")
        }

        val awDelta = compareAbs(baseline.beanAw, input.aw)
        when {
            awDelta == null -> reasons.add("aw comparison unavailable")
            awDelta <= 0.02 -> {
                score += 10
                reasons.add("aw is very close")
            }
            awDelta <= 0.05 -> {
                score += 5
                reasons.add("aw is moderately close")
            }
            else -> reasons.add("aw differs")
        }

        val baselineRoastLevel = baseline.roastLevel.orEmpty()
        val inputRoastLevel = input.roastLevel.orEmpty()
        if (baselineRoastLevel.isNotBlank() && inputRoastLevel.isNotBlank()) {
            if (baselineRoastLevel.equals(inputRoastLevel, ignoreCase = true)) {
                score += 8
                reasons.add("Roast level target matches")
            } else {
                reasons.add("Roast level target differs")
            }
        }

        val baselineOrientation = baseline.orientation.orEmpty()
        val inputOrientation = input.orientation.orEmpty()
        if (baselineOrientation.isNotBlank() && inputOrientation.isNotBlank()) {
            if (baselineOrientation.equals(inputOrientation, ignoreCase = true)) {
                score += 7
                reasons.add("Orientation matches")
            } else {
                reasons.add("Orientation differs")
            }
        }

        val grade = when {
            score >= 75 -> BaselineMatchGrade.EXACT_MATCH
            score >= 45 -> BaselineMatchGrade.SIMILAR_MATCH
            else -> BaselineMatchGrade.REFERENCE_ONLY
        }

        val summary = """
Planner Baseline Match

Grade
${gradeLabel(grade)}

Score
$score

Reasons
${reasons.joinToString("\n") { "• $it" }}
        """.trimIndent()

        return PlannerBaselineMatchResult(
            grade = grade,
            score = score,
            reasons = reasons,
            summary = summary
        )
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

        val match = evaluateMatchAgainstCurrentInput()

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

Match Grade
${match?.let { gradeLabel(it.grade) } ?: "Unavailable"}

Match Score
${match?.score?.toString() ?: "-"}
        """.trimIndent()
    }

    private fun compareAbs(a: Double?, b: Double?): Double? {
        if (a == null || b == null) return null
        return abs(a - b)
    }

    private fun processFamily(process: String): String {
        val p = process.lowercase()
        return when {
            "washed" in p -> "washed"
            "natural" in p || "dry" in p -> "natural"
            "honey" in p -> "honey"
            "anaerobic" in p -> "anaerobic"
            else -> p
        }
    }

    private fun gradeLabel(grade: BaselineMatchGrade): String {
        return when (grade) {
            BaselineMatchGrade.EXACT_MATCH -> "Exact Match"
            BaselineMatchGrade.SIMILAR_MATCH -> "Similar Match"
            BaselineMatchGrade.REFERENCE_ONLY -> "Reference Only"
        }
    }
}
