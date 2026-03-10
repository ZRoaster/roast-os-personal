package com.roastos.app

data class RoastValidationIssue(
    val code: String,
    val title: String,
    val detail: String,
    val severity: String
)

data class RoastValidationResult(
    val issues: List<RoastValidationIssue>
) {
    fun hasIssues(): Boolean {
        return issues.isNotEmpty()
    }

    fun highestSeverity(): String {
        return when {
            issues.any { it.severity == "high" } -> "high"
            issues.any { it.severity == "medium" } -> "medium"
            issues.any { it.severity == "watch" } -> "watch"
            else -> "none"
        }
    }

    fun summary(): String {
        if (issues.isEmpty()) {
            return """
Validation
No major risk detected.

Level
none
            """.trimIndent()
        }

        return buildString {
            appendLine("Validation")
            appendLine()
            issues.forEachIndexed { index, issue ->
                appendLine("${index + 1}. ${issue.title}")
                appendLine(issue.detail)
                appendLine("Level: ${issue.severity}")
                if (index != issues.lastIndex) {
                    appendLine()
                }
            }
        }
    }
}

object RoastSessionValidator {

    fun validate(
        snapshot: RoastSessionBusSnapshot
    ): RoastValidationResult {

        val session = snapshot.session
        val issues = mutableListOf<RoastValidationIssue>()

        val beanTemp = session.lastBeanTemp
        val ror = session.lastRor
        val elapsed = session.lastElapsedSec

        if (session.status != RoastSessionStatus.RUNNING) {
            return RoastValidationResult(emptyList())
        }

        if (elapsed >= 30 && ror < 3.0) {
            issues.add(
                RoastValidationIssue(
                    code = "stall",
                    title = "Possible Stall",
                    detail = "RoR is very low after the opening phase. The roast may lose internal momentum.",
                    severity = "medium"
                )
            )
        }

        if (elapsed in 45..360 && ror < 2.0) {
            issues.add(
                RoastValidationIssue(
                    code = "low_energy",
                    title = "Low Energy State",
                    detail = "The roast is carrying weak energy into the middle section. Structure may flatten later.",
                    severity = "watch"
                )
            )
        }

        if (beanTemp in 120.0..190.0 && ror > 12.5) {
            issues.add(
                RoastValidationIssue(
                    code = "high_energy",
                    title = "High Energy State",
                    detail = "RoR is strong in the middle section. Sweetness may build well, but harshness risk is increasing.",
                    severity = "watch"
                )
            )
        }

        if (beanTemp >= 175.0 && ror < 1.5) {
            issues.add(
                RoastValidationIssue(
                    code = "crash",
                    title = "Crash Risk",
                    detail = "RoR is collapsing late in the roast. The finish may become flat or muted.",
                    severity = "high"
                )
            )
        }

        if (beanTemp >= 185.0 && ror > 10.0) {
            issues.add(
                RoastValidationIssue(
                    code = "flick",
                    title = "Flick Risk",
                    detail = "RoR is rising aggressively in the final stage. The finish may become sharp or less refined.",
                    severity = "medium"
                )
            )
        }

        return RoastValidationResult(
            issues = issues.distinctBy { it.code }
        )
    }
}
