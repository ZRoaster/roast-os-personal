package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastEvaluation
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.UiKit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object RoastComparePage {

    fun show(
        context: Context,
        container: LinearLayout,
        left: RoastHistoryEntry?,
        right: RoastHistoryEntry?,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST COMPARE"))
        root.addView(UiKit.pageSubtitle(context, "Inspect difference, result, and reuse value"))
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.REVIEW
            )
        )
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "Back")
        accessCard.addView(UiKit.cardTitle(context, "ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Return to the review flow."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(backBtn)
        root.addView(accessCard)
        root.addView(UiKit.spacer(context))

        if (left == null || right == null) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO COMPARE DATA"))
            emptyCard.addView(UiKit.helperText(context, "Two roast history entries are required for comparison."))
            root.addView(emptyCard)

            backBtn.setOnClickListener {
                onBack?.invoke() ?: ReviewHubPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val headlineCard = UiKit.card(context)
        headlineCard.addView(UiKit.cardTitle(context, "COMPARE HEADLINE"))
        headlineCard.addView(UiKit.spacerS(context))
        headlineCard.addView(
            UiKit.bodyText(
                context,
                buildCompareHeadline(left, right)
            )
        )
        root.addView(headlineCard)
        root.addView(UiKit.spacer(context))

        val differenceCard = UiKit.card(context)
        differenceCard.addView(UiKit.cardTitle(context, "KEY DIFFERENCES"))
        differenceCard.addView(UiKit.spacerS(context))
        differenceCard.addView(
            UiKit.bodyText(
                context,
                buildKeyDifferences(left, right)
            )
        )
        root.addView(differenceCard)
        root.addView(UiKit.spacer(context))

        val hintCard = UiKit.card(context)
        hintCard.addView(UiKit.cardTitle(context, "ACTIONABLE HINT"))
        hintCard.addView(UiKit.spacerS(context))
        hintCard.addView(
            UiKit.bodyText(
                context,
                buildActionableHint(left, right)
            )
        )
        root.addView(hintCard)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "FAST SUMMARY"))
        summaryCard.addView(UiKit.spacerS(context))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                buildFastCompareStrip(left, right)
            )
        )
        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val batchCard = UiKit.card(context)
        batchCard.addView(UiKit.cardTitle(context, "BATCH OVERVIEW"))
        batchCard.addView(UiKit.spacerS(context))
        batchCard.addView(
            UiKit.bodyText(
                context,
                """
A
${buildBatchOverview(left)}

B
${buildBatchOverview(right)}
                """.trimIndent()
            )
        )
        root.addView(batchCard)
        root.addView(UiKit.spacer(context))

        val timelineCard = UiKit.card(context)
        timelineCard.addView(UiKit.cardTitle(context, "TIMELINE"))
        timelineCard.addView(UiKit.spacerS(context))
        timelineCard.addView(
            UiKit.bodyText(
                context,
                """
A
${buildTimeline(left)}

B
${buildTimeline(right)}
                """.trimIndent()
            )
        )
        root.addView(timelineCard)
        root.addView(UiKit.spacer(context))

        val environmentCard = UiKit.card(context)
        environmentCard.addView(UiKit.cardTitle(context, "ENVIRONMENT"))
        environmentCard.addView(UiKit.spacerS(context))
        environmentCard.addView(
            UiKit.bodyText(
                context,
                """
A
${buildEnvironment(left)}

B
${buildEnvironment(right)}
                """.trimIndent()
            )
        )
        root.addView(environmentCard)
        root.addView(UiKit.spacer(context))

        val evaluationCard = UiKit.card(context)
        evaluationCard.addView(UiKit.cardTitle(context, "EVALUATION"))
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(
            UiKit.bodyText(
                context,
                buildEvaluationDifferences(left.evaluation, right.evaluation)
            )
        )
        root.addView(evaluationCard)
        root.addView(UiKit.spacer(context))

        val notesCard = UiKit.card(context)
        notesCard.addView(UiKit.cardTitle(context, "INSIGHT NOTES"))
        notesCard.addView(UiKit.spacerS(context))
        notesCard.addView(
            UiKit.bodyText(
                context,
                """
A Report
${left.reportText.ifBlank { "-" }}

A Diagnosis
${left.diagnosisText.ifBlank { "-" }}

A Correction
${left.correctionText.ifBlank { "-" }}

B Report
${right.reportText.ifBlank { "-" }}

B Diagnosis
${right.diagnosisText.ifBlank { "-" }}

B Correction
${right.correctionText.ifBlank { "-" }}
                """.trimIndent()
            )
        )
        root.addView(notesCard)

        backBtn.setOnClickListener {
            onBack?.invoke() ?: ReviewHubPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildCompareHeadline(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val fcA = left.actualFcSec ?: left.predictedFcSec
        val fcB = right.actualFcSec ?: right.predictedFcSec
        val dropA = left.actualDropSec ?: left.predictedDropSec
        val dropB = right.actualDropSec ?: right.predictedDropSec

        val fcDiff = diffIfBothPresent(fcA, fcB)
        val dropDiff = diffIfBothPresent(dropA, dropB)
        val riskA = riskScore(left.roastHealthHeadline)
        val riskB = riskScore(right.roastHealthHeadline)

        val headline = when {
            fcDiff != null && abs(fcDiff) >= 10 -> {
                if (fcDiff < 0) {
                    "A enters first crack clearly earlier than B."
                } else {
                    "B enters first crack clearly earlier than A."
                }
            }
            dropDiff != null && abs(dropDiff) >= 10 -> {
                if (dropDiff < 0) {
                    "A finishes earlier than B."
                } else {
                    "B finishes earlier than A."
                }
            }
            riskA != riskB -> {
                if (riskA > riskB) {
                    "A shows a weaker roast health outcome than B."
                } else {
                    "B shows a weaker roast health outcome than A."
                }
            }
            else -> "These two roasts are broadly comparable at first glance."
        }

        val reuse = when {
            strongGapCount(left, right) >= 3 -> "Reuse confidence is low."
            strongGapCount(left, right) >= 1 -> "Reuse confidence is conditional."
            else -> "Reuse confidence is relatively high."
        }

        return """
Headline
$headline

Reuse
$reuse

Compare Pair
${left.batchId} ↔ ${right.batchId}
        """.trimIndent()
    }

    private fun buildKeyDifferences(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val tags = mutableListOf<String>()

        addTimeDifference(tags, "Turning", left.actualTurningSec ?: left.predictedTurningSec, right.actualTurningSec ?: right.predictedTurningSec)
        addTimeDifference(tags, "Yellow", left.actualYellowSec ?: left.predictedYellowSec, right.actualYellowSec ?: right.predictedYellowSec)
        addTimeDifference(tags, "First Crack", left.actualFcSec ?: left.predictedFcSec, right.actualFcSec ?: right.predictedFcSec)
        addTimeDifference(tags, "Drop", left.actualDropSec ?: left.predictedDropSec, right.actualDropSec ?: right.predictedDropSec)

        val rorDiff = diffIfBothPresent(left.actualPreFcRor, right.actualPreFcRor)
        if (rorDiff != null && abs(rorDiff) >= 0.5) {
            tags += if (rorDiff > 0) {
                "A has higher pre-FC RoR than B."
            } else {
                "B has higher pre-FC RoR than A."
            }
        }

        if (abs(left.envTemp - right.envTemp) >= 1.0) {
            tags += if (left.envTemp > right.envTemp) {
                "A was roasted in a warmer environment."
            } else {
                "B was roasted in a warmer environment."
            }
        }

        if (abs(left.envRh - right.envRh) >= 5.0) {
            tags += if (left.envRh > right.envRh) {
                "A had higher ambient humidity."
            } else {
                "B had higher ambient humidity."
            }
        }

        val riskA = riskScore(left.roastHealthHeadline)
        val riskB = riskScore(right.roastHealthHeadline)
        if (riskA != riskB) {
            tags += if (riskA > riskB) {
                "A carries a weaker roast health headline."
            } else {
                "B carries a weaker roast health headline."
            }
        }

        return if (tags.isEmpty()) {
            "No strong difference is detected under the current compare rules."
        } else {
            tags.joinToString("\n")
        }
    }

    private fun buildActionableHint(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        val hints = mutableListOf<String>()

        val fcDiff = diffIfBothPresent(left.actualFcSec ?: left.predictedFcSec, right.actualFcSec ?: right.predictedFcSec)
        if (fcDiff != null && abs(fcDiff) >= 10) {
            hints += if (fcDiff < 0) {
                "If you want to repeat A, verify whether mid-late phase energy was intentionally stronger."
            } else {
                "If you want to repeat B, verify whether mid-late phase energy was intentionally stronger."
            }
        }

        val dropDiff = diffIfBothPresent(left.actualDropSec ?: left.predictedDropSec, right.actualDropSec ?: right.predictedDropSec)
        if (dropDiff != null && abs(dropDiff) >= 10) {
            hints += "Check whether the finish window difference was intentional before replaying either roast."
        }

        if (abs(left.envTemp - right.envTemp) >= 1.0 || abs(left.envRh - right.envRh) >= 5.0) {
            hints += "Do not compare heat application directly without accounting for the environment shift."
        }

        val riskA = riskScore(left.roastHealthHeadline)
        val riskB = riskScore(right.roastHealthHeadline)
        if (riskA != riskB) {
            hints += "Use the healthier roast as the first replay reference unless cup evaluation says otherwise."
        }

        if (hints.isEmpty()) {
            return "Treat these two roasts as broadly comparable and inspect detailed sections for smaller differences."
        }

        return hints.take(4).joinToString("\n")
    }

    private fun buildFastCompareStrip(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): String {
        return """
A
${left.batchId}

FC / Drop
${formatSec(left.actualFcSec ?: left.predictedFcSec)} / ${formatSec(left.actualDropSec ?: left.predictedDropSec)}

Health / Evaluation
${left.roastHealthHeadline} / ${if (left.evaluation != null) "Saved" else "Not saved"}

B
${right.batchId}

FC / Drop
${formatSec(right.actualFcSec ?: right.predictedFcSec)} / ${formatSec(right.actualDropSec ?: right.predictedDropSec)}

Health / Evaluation
${right.roastHealthHeadline} / ${if (right.evaluation != null) "Saved" else "Not saved"}
        """.trimIndent()
    }

    private fun buildBatchOverview(
        entry: RoastHistoryEntry
    ): String {
        return """
批次
${entry.batchId}

标题 / 处理
${entry.title} / ${entry.process}

创建时间
${formatDateTime(entry.createdAtMillis)}

结果
${entry.batchStatus}
        """.trimIndent()
    }

    private fun buildEnvironment(
        entry: RoastHistoryEntry
    ): String {
        return """
环境温度 / 湿度
${entry.envTemp} ℃ / ${entry.envRh} %

密度 / 水分 / AW
${entry.density} / ${entry.moisture} / ${entry.aw}
        """.trimIndent()
    }

    private fun buildTimeline(
        entry: RoastHistoryEntry
    ): String {
        return """
Turning / Yellow
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)} / ${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

FC / Drop
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)} / ${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}

Pre-FC RoR
${formatRor(entry.actualPreFcRor)}
        """.trimIndent()
    }

    private fun buildEvaluationDifferences(
        left: RoastEvaluation?,
        right: RoastEvaluation?
    ): String {
        if (left == null && right == null) {
            return "No saved evaluation on either roast."
        }

        return """
A Evaluation
${buildEvaluationSummary(left)}

B Evaluation
${buildEvaluationSummary(right)}
        """.trimIndent()
    }

    private fun buildEvaluationSummary(
        evaluation: RoastEvaluation?
    ): String {
        if (evaluation == null) return "Not saved"

        return """
Bean / Ground / AW
${evaluation.beanColor ?: "-"} / ${evaluation.groundColor ?: "-"} / ${evaluation.roastedAw ?: "-"}

Sweetness / Acidity / Body
${evaluation.sweetness ?: "-"} / ${evaluation.acidity ?: "-"} / ${evaluation.body ?: "-"}

Clarity / Balance
${evaluation.flavorClarity ?: "-"} / ${evaluation.balance ?: "-"}

Notes
${evaluation.notes.ifBlank { "-" }}
        """.trimIndent()
    }

    private fun addTimeDifference(
        tags: MutableList<String>,
        label: String,
        leftSec: Int?,
        rightSec: Int?
    ) {
        val diff = diffIfBothPresent(leftSec, rightSec)
        if (diff != null && abs(diff) >= 10) {
            tags += if (diff < 0) {
                "A reaches $label earlier than B."
            } else {
                "B reaches $label earlier than A."
            }
        }
    }

    private fun strongGapCount(
        left: RoastHistoryEntry,
        right: RoastHistoryEntry
    ): Int {
        val checks = listOf(
            abs((left.envTemp) - (right.envTemp)) >= 1.0,
            abs((left.envRh) - (right.envRh)) >= 5.0,
            abs((left.actualYellowSec ?: left.predictedYellowSec ?: 0) - (right.actualYellowSec ?: right.predictedYellowSec ?: 0)) >= 10,
            abs((left.actualFcSec ?: left.predictedFcSec ?: 0) - (right.actualFcSec ?: right.predictedFcSec ?: 0)) >= 10,
            abs((left.actualDropSec ?: left.predictedDropSec ?: 0) - (right.actualDropSec ?: right.predictedDropSec ?: 0)) >= 10,
            abs((left.actualPreFcRor ?: 0.0) - (right.actualPreFcRor ?: 0.0)) >= 0.5,
            abs(riskScore(left.roastHealthHeadline) - riskScore(right.roastHealthHeadline)) >= 2
        )
        return checks.count { it }
    }

    private fun riskScore(headline: String): Int {
        val text = headline.lowercase(Locale.getDefault())
        return when {
            "高风险" in headline -> 4
            "中风险" in headline -> 3
            "需留意" in headline -> 2
            "低风险" in headline -> 1
            "high" in text -> 4
            "medium" in text -> 3
            "watch" in text -> 2
            "low" in text -> 1
            else -> 0
        }
    }

    private fun diffIfBothPresent(left: Int?, right: Int?): Int? {
        if (left == null || right == null) return null
        return left - right
    }

    private fun diffIfBothPresent(left: Double?, right: Double?): Double? {
        if (left == null || right == null) return null
        return left - right
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRor(value: Double?): String {
        if (value == null) return "-"
        return String.format(Locale.getDefault(), "%.1f ℃/min", value)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
